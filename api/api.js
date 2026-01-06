// api.js (1 file)
// NodeJS + Express + Aiven MySQL (SSL CA) + JWT + OTP email
require("dotenv").config();

const fs = require("fs");
const path = require("path");
const express = require("express");
const cors = require("cors");
const bcrypt = require("bcryptjs");
const jwt = require("jsonwebtoken");
const nodemailer = require("nodemailer");
const mysql = require("mysql2/promise");

const app = express();
app.use(cors());
app.use(express.json());

// ------------------ MYSQL (Aiven) ------------------
const caPath = process.env.DB_SSL_CA
  ? path.resolve(__dirname, process.env.DB_SSL_CA)
  : path.resolve(__dirname, "ca.pem"); // fallback

const pool = mysql.createPool({
  host: process.env.DB_HOST,
  port: Number(process.env.DB_PORT || 3306),
  user: process.env.DB_USER,
  password: process.env.DB_PASS,
  database: process.env.DB_NAME,
  waitForConnections: true,
  connectionLimit: 10,
  ssl: {
    ca: fs.readFileSync(caPath, "utf8"),
    rejectUnauthorized: true,
  },
});

// ------------------ MAIL (Gmail App Password) ------------------
const transporter = nodemailer.createTransport({
  service: "gmail",
  auth: {
    user: process.env.SMTP_USER,
    pass: process.env.SMTP_PASS, // Gmail App Password
  },
});

async function sendOtpEmail(toEmail, otp) {
  const from = process.env.SMTP_FROM || process.env.SMTP_USER;
  await transporter.sendMail({
    from: `"No Reply" <${from}>`,
    to: toEmail,
    subject: "Your OTP code",
    text: `Your OTP code is: ${otp}\nThis code is valid for 5 minutes.`,
  });
}

// ------------------ HELPERS ------------------
function ok(res, data = null, extra = {}) {
  return res.json({ success: true, ...(data !== null ? { data } : {}), ...extra });
}
function fail(res, code, message) {
  return res.status(code).json({ success: false, error: message });
}
function genOtp() {
  return String(Math.floor(Math.random() * 1000000)).padStart(6, "0");
}
function genToken() {
  return require("crypto").randomBytes(20).toString("hex");
}
function signJwt(account) {
  const secret = process.env.JWT_SECRET || "dev_secret_change_me";
  return jwt.sign({ account }, secret, { expiresIn: "7d" });
}
function authMiddleware(req, res, next) {
  const h = req.headers.authorization || "";
  const token = h.startsWith("Bearer ") ? h.slice(7) : null;
  if (!token) return fail(res, 401, "Not authenticated");

  try {
    const secret = process.env.JWT_SECRET || "dev_secret_change_me";
    const decoded = jwt.verify(token, secret);
    req.account = decoded.account;
    next();
  } catch {
    return fail(res, 401, "Invalid token");
  }
}

// ------------------ ROUTE: /api.php?action=... ------------------
app.all("/api.php", async (req, res) => {
  const action = (req.query.action || "").toString();

  try {
    // ---------- AUTH ----------
    if (action === "register") {
      const { account, password } = req.body || {};
      if (!account || !password) return fail(res, 400, "account and password required");

      const [ex] = await pool.query("SELECT id FROM users WHERE account=? LIMIT 1", [account]);
      if (ex.length) return fail(res, 400, "Account already exists");

      const hash = await bcrypt.hash(password, 10);
      await pool.query("INSERT INTO users(account,password_hash) VALUES(?,?)", [account, hash]);

      const token = signJwt(account);
      return ok(res, { account, token });
    }

    if (action === "login") {
      const { account, password } = req.body || {};
      if (!account || !password) return fail(res, 400, "account and password required");

      const [rows] = await pool.query(
        "SELECT id, password_hash FROM users WHERE account=? LIMIT 1",
        [account]
      );
      if (!rows.length) return fail(res, 401, "Invalid credentials");

      const okPass = await bcrypt.compare(password, rows[0].password_hash);
      if (!okPass) return fail(res, 401, "Invalid credentials");

      const token = signJwt(account);
      return ok(res, { account, token });
    }

    // ---------- PRODUCTS ----------
    if (action === "products") {
      // tuỳ DB bạn: bảng products / categories / shops...
      const [rows] = await pool.query(
        "SELECT id, name AS ProductName, category AS Category, image_url AS ImageURL FROM products ORDER BY id"
      );
      return ok(res, rows, { count: rows.length });
    }

    // ---------- PROFILE (JWT) ----------
    if (action === "profile_get") {
      // yêu cầu Authorization: Bearer ...
      return authMiddleware(req, res, async () => {
        const account = req.account;
        const [rows] = await pool.query(
          "SELECT id AS ProfileID, account AS Account, first_name AS FirstName, last_name AS LastName, avatar_url AS ImageURL FROM users WHERE account=? LIMIT 1",
          [account]
        );
        if (!rows.length) return fail(res, 404, "Profile not found");
        return ok(res, rows[0]);
      });
    }

    // ---------- FORGOT / OTP ----------
    if (action === "forgot") {
      const { email } = req.body || {};
      if (!email) return fail(res, 400, "email required");

      // ở đây coi account = email (nếu bạn tách email riêng thì đổi query)
      const otp = genOtp();
      const resetToken = genToken();
      const now = new Date();
      const otpExpires = new Date(now.getTime() + 5 * 60 * 1000);
      const resetExpires = new Date(now.getTime() + 15 * 60 * 1000);

      const [rows] = await pool.query("SELECT id FROM users WHERE account=? LIMIT 1", [email]);
      if (rows.length) {
        await pool.query(
          "UPDATE users SET otp=?, otp_expires=?, reset_token=?, reset_expires=? WHERE id=?",
          [otp, otpExpires, resetToken, resetExpires, rows[0].id]
        );

        try { await sendOtpEmail(email, otp); } catch (e) { console.error("send mail fail", e); }
      }

      // luôn trả success để tránh lộ email tồn tại hay không
      return ok(res, null, { message: "If the email exists, an OTP was sent" });
    }

    if (action === "verify-otp") {
      const { email, otp } = req.body || {};
      if (!email || !otp) return fail(res, 400, "email and otp required");

      const [rows] = await pool.query(
        "SELECT id, otp AS OTP, otp_expires AS OtpExpires, reset_token AS ResetToken, reset_expires AS ResetExpires FROM users WHERE account=? LIMIT 1",
        [email]
      );
      if (!rows.length || !rows[0].OTP) return fail(res, 400, "Invalid OTP or no request found");

      if (new Date(rows[0].OtpExpires).getTime() < Date.now()) {
        await pool.query("UPDATE users SET otp=NULL, otp_expires=NULL WHERE id=?", [rows[0].id]);
        return fail(res, 400, "OTP expired");
      }

      if (String(rows[0].OTP) !== String(otp)) return fail(res, 400, "Invalid OTP");

      // clear OTP after use
      await pool.query("UPDATE users SET otp=NULL, otp_expires=NULL WHERE id=?", [rows[0].id]);

      return ok(res, null, { message: "OTP verified", resetToken: rows[0].ResetToken });
    }

    if (action === "reset-password") {
      const { resetToken, newPassword } = req.body || {};
      if (!resetToken || !newPassword) return fail(res, 400, "resetToken and newPassword required");

      const [rows] = await pool.query(
        "SELECT id, reset_expires AS ResetExpires FROM users WHERE reset_token=? LIMIT 1",
        [resetToken]
      );
      if (!rows.length) return fail(res, 400, "Invalid reset token");

      if (new Date(rows[0].ResetExpires).getTime() < Date.now()) {
        return fail(res, 400, "Reset token expired");
      }

      const hash = await bcrypt.hash(newPassword, 10);
      await pool.query(
        "UPDATE users SET password_hash=?, reset_token=NULL, reset_expires=NULL WHERE id=?",
        [hash, rows[0].id]
      );

      return ok(res, null, { message: "Password updated" });
    }

    return fail(res, 400, "Invalid or missing action");
  } catch (e) {
    console.error(e);
    return res.status(500).json({ success: false, error: "Server error", message: e.message });
  }
});

// ------------------ START ------------------
const PORT = process.env.PORT || 3000;
app.listen(PORT, () => console.log(`API running on :${PORT}`));
