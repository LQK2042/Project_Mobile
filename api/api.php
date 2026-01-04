<?php

// Đặt ở đầu api.php (sau session_start() nếu bạn muốn session trước các include nào cần session)
// Manual include PHPMailer (không dùng composer)
require_once __DIR__ . '/phpmailer/src/Exception.php';
require_once __DIR__ . '/phpmailer/src/PHPMailer.php';
require_once __DIR__ . '/phpmailer/src/SMTP.php';

use PHPMailer\PHPMailer\PHPMailer;
use PHPMailer\PHPMailer\Exception as PHPMailerException; // alias để tránh xung đột

/**
 * Gửi OTP bằng PHPMailer qua SMTP (Gmail App Password)
 * Không dùng Composer - dùng các file trong phpmailer/src
 */
function sendOtpEmailSmtp($toEmail, $otp) {
    $host = getenv('SMTP_HOST') ?: 'smtp.gmail.com';
    $port = intval(getenv('SMTP_PORT') ?: 587);
    $username = getenv('SMTP_USER') ?: 'mhuyy.ho@gmail.com'; // thay bằng email bạn (tốt nhất: env)
    $password = getenv('SMTP_PASS') ?: 'mmexydfgbzbcnohb';  // App Password (tốt nhất: env)
    $from = getenv('SMTP_FROM') ?: $username;

    $mail = new PHPMailer(true);
    try {
        // Server settings
        $mail->isSMTP();
        $mail->Host       = $host;
        $mail->SMTPAuth   = true;
        $mail->Username   = $username;
        $mail->Password   = $password;
        $mail->SMTPSecure = PHPMailer::ENCRYPTION_STARTTLS; // STARTTLS
        $mail->Port       = $port;

        // Optional: debug for dev
        // $mail->SMTPDebug = 2; // chỉ bật khi debug

        // Recipients
        $mail->setFrom($from, 'No Reply');
        $mail->addAddress($toEmail);

        // Content
        $mail->isHTML(false);
        $mail->Subject = 'Your OTP code';
        $mail->Body    = "Your OTP code is: {$otp}\nThis code is valid for 5 minutes.";

        $mail->send();
        return true;
    } catch (PHPMailerException $e) {
        error_log('[PHPMailer] Send failed: ' . $e->getMessage());
        return false;
    }
} 

// api.php
header('Content-Type: application/json; charset=utf-8');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type, Authorization, X-Requested-With');

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(204);
    exit;
}

ini_set('display_errors', 0);
date_default_timezone_set('UTC');
session_start();

// --- Configuration (read from env or fallback) ---
$DB_HOST = getenv('DB_HOST') ?: 'dbmysqlserver36.mysql.database.azure.com';
$DB_PORT = getenv('DB_PORT') ?: 3306;
$DB_NAME = getenv('DB_NAME') ?: 'manga_app';
$DB_USER = getenv('DB_USER') ?: 'admin123';
$DB_PASS = getenv('DB_PASSWORD') ?: getenv('DB_PASS') ?: 'so01VVN@@@';
$CA_FILE = __DIR__ . '/BaltimoreCyberTrustRoot.crt.pem';

// DSNs with SSL modes
$dsn_verify_ca = "mysql:host={$DB_HOST};port={$DB_PORT};dbname={$DB_NAME};charset=utf8mb4;ssl-mode=VERIFY_CA";
$dsn_required  = "mysql:host={$DB_HOST};port={$DB_PORT};dbname={$DB_NAME};charset=utf8mb4;ssl-mode=REQUIRED";

$baseOptions = [
    PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
    PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
];

function addCaIfPossible(array $opts, string $caPath): array {
    if (defined('PDO::MYSQL_ATTR_SSL_CA') && file_exists($caPath)) {
        $opts[PDO::MYSQL_ATTR_SSL_CA] = $caPath;
    }
    return $opts;
}

// Try connections (VERIFY_CA -> REQUIRED -> REQUIRED with VERIFY_SERVER_CERT=false)
$pdo = null;
$lastException = null;

try {
    $opts1 = addCaIfPossible($baseOptions, $CA_FILE);
    $pdo = new PDO($dsn_verify_ca, $DB_USER, $DB_PASS, $opts1);
} catch (\PDOException $e) {
    $lastException = $e;
}

if (!$pdo) {
    try {
        $opts2 = addCaIfPossible($baseOptions, $CA_FILE);
        $pdo = new PDO($dsn_required, $DB_USER, $DB_PASS, $opts2);
    } catch (\PDOException $e) {
        $lastException = $e;
    }
}

if (!$pdo) {
    try {
        $opts3 = addCaIfPossible($baseOptions, $CA_FILE);
        if (defined('PDO::MYSQL_ATTR_SSL_VERIFY_SERVER_CERT')) {
            $opts3[PDO::MYSQL_ATTR_SSL_VERIFY_SERVER_CERT] = false;
        }
        $pdo = new PDO($dsn_required, $DB_USER, $DB_PASS, $opts3);
    } catch (\PDOException $e) {
        $lastException = $e;
    }
}

if (!$pdo) {
    http_response_code(500);
    echo json_encode([
        'success' => false,
        'error' => 'DB connection failed',
        'message' => $lastException ? $lastException->getMessage() : 'unknown error',
        'help' => [
            "ensure DB_USER is user@servername (e.g. admin123@dbmysqlserver36)",
            "ensure Azure MySQL firewall allows your public IP",
            "place BaltimoreCyberTrustRoot.crt.pem in this folder if using VERIFY_CA",
            "test with MySQL CLI: mysql -h {$DB_HOST} -P {$DB_PORT} -u '{$DB_USER}' -p --ssl-mode=REQUIRED"
        ]
    ], JSON_UNESCAPED_UNICODE);
    exit;
}

// --- Helpers ---
function jsonInput() {
    $raw = file_get_contents('php://input');
    $data = json_decode($raw, true);
    return is_array($data) ? $data : [];
}
function requireAuth() {
    if (empty($_SESSION['account'])) {
        http_response_code(401);
        echo json_encode(['success' => false, 'error' => 'Not authenticated'], JSON_UNESCAPED_UNICODE);
        exit;
    }
}
function getCurrentAccount() {
    return $_SESSION['account'] ?? null;
}

// Helper: tạo OTP 6 chữ số
function genOtp() {
    // 6 chữ số, zero-padded
    return str_pad((string)random_int(0, 999999), 6, '0', STR_PAD_LEFT);
}

// Helper: tạo reset token (hex)
function genToken() {
    return bin2hex(random_bytes(20));
}


// --- Routing ---
$action = $_GET['action'] ?? null;

switch ($action) {
    case 'products':
        // optional ?category=categoryName
        $category = isset($_GET['category']) ? trim($_GET['category']) : null;
        getProducts($pdo, $category);
        break;

    case 'products_by_category':
        // accepts ?category=categoryName (or POST JSON {category:...})
        $category = $_GET['category'] ?? (jsonInput()['category'] ?? null);
        if (!$category) {
            http_response_code(400);
            echo json_encode(['success' => false, 'error' => 'category is required'], JSON_UNESCAPED_UNICODE);
            exit;
        }
        getProducts($pdo, $category);
        break;

    case 'register':
        $body = jsonInput();
        registerUser($pdo, $body);
        break;

    case 'login':
        $body = jsonInput();
        loginUser($pdo, $body);
        break;

    case 'logout':
        logoutUser();
        break;

    case 'forgot':
        $body = jsonInput();
        $email = trim($body['email'] ?? '');
        if ($email === '') {
            http_response_code(400);
            echo json_encode(['success' => false, 'message' => 'email required'], JSON_UNESCAPED_UNICODE);
            break;
        }

        // tìm user theo Account (tùy schema: nếu Account không phải email thì map sang email từ UserDB)
        $stmt = $pdo->prepare('SELECT ID FROM UserApp WHERE Account = ? LIMIT 1');
        $stmt->execute([$email]);
        $row = $stmt->fetch();

        $otp = genOtp();
        $resetToken = genToken();
        $now = date('Y-m-d H:i:s');
        $resetExpires = date('Y-m-d H:i:s', time() + 15*60); // 15 phút

        if ($row) {
            $stmt = $pdo->prepare('UPDATE UserApp SET OTP = ?, DateReceive = ?, ResetToken = ?, ResetExpires = ? WHERE ID = ?');
            $stmt->execute([$otp, $now, $resetToken, $resetExpires, $row['ID']]);

            // Gửi OTP bằng PHPMailer (manual) nếu cấu hình, ngược lại fallback (mail() hoặc log)
            $sent = sendOtpEmailSmtp($email, $otp);
            if (!$sent) {
                error_log("[API] PHPMailer failed to send OTP to {$email}");
                // vẫn trả success để tránh enumeration
            }
        } else {
            // Không tìm thấy account => vẫn trả success để tránh lộ thông tin
            error_log("[API] forgot requested for unknown account: {$email}");
        }

        echo json_encode(['success' => true, 'message' => 'If the email exists, an OTP was sent'], JSON_UNESCAPED_UNICODE);
        break;

    case 'verify-otp':
        $body = jsonInput();
        $email = trim($body['email'] ?? '');
        $otp = trim($body['otp'] ?? '');
        if ($email === '' || $otp === '') {
            http_response_code(400);
            echo json_encode(['success' => false, 'message' => 'email and otp required'], JSON_UNESCAPED_UNICODE);
            break;
        }

        $stmt = $pdo->prepare('SELECT ID, OTP, DateReceive, ResetToken, ResetExpires FROM UserApp WHERE Account = ? LIMIT 1');
        $stmt->execute([$email]);
        $row = $stmt->fetch();
        if (!$row || !$row['OTP']) {
            http_response_code(400);
            echo json_encode(['success' => false, 'message' => 'Invalid OTP or no request found'], JSON_UNESCAPED_UNICODE);
            break;
        }

        $otpTime = strtotime($row['DateReceive']);
        if ($otpTime === false || time() > $otpTime + 5*60) {
            // clear OTP
            $stmt = $pdo->prepare('UPDATE UserApp SET OTP = NULL, DateReceive = NULL WHERE ID = ?');
            $stmt->execute([$row['ID']]);
            http_response_code(400);
            echo json_encode(['success' => false, 'message' => 'OTP expired'], JSON_UNESCAPED_UNICODE);
            break;
        }

        if (!hash_equals($row['OTP'], $otp)) {
            http_response_code(400);
            echo json_encode(['success' => false, 'message' => 'Invalid OTP'], JSON_UNESCAPED_UNICODE);
            break;
        }

        // OTP đúng -> đảm bảo có reset token
        $resetToken = $row['ResetToken'] ?? null;
        $resetExpires = $row['ResetExpires'] ?? date('Y-m-d H:i:s', time() + 15*60);
        if (!$resetToken) {
            $resetToken = genToken();
            $resetExpires = date('Y-m-d H:i:s', time() + 15*60);
            $stmt = $pdo->prepare('UPDATE UserApp SET ResetToken = ?, ResetExpires = ? WHERE ID = ?');
            $stmt->execute([$resetToken, $resetExpires, $row['ID']]);
        }

        // xóa OTP sau khi sử dụng
        $stmt = $pdo->prepare('UPDATE UserApp SET OTP = NULL, DateReceive = NULL WHERE ID = ?');
        $stmt->execute([$row['ID']]);

        echo json_encode(['success' => true, 'message' => 'OTP verified', 'resetToken' => $resetToken], JSON_UNESCAPED_UNICODE);
        break;

    case 'reset-password':
        $body = jsonInput();
        $resetToken = trim($body['resetToken'] ?? '');
        $newPassword = $body['newPassword'] ?? '';
        if ($resetToken === '' || $newPassword === '') {
            http_response_code(400);
            echo json_encode(['success' => false, 'message' => 'resetToken and newPassword required'], JSON_UNESCAPED_UNICODE);
            break;
        }

        $stmt = $pdo->prepare('SELECT ID, ResetExpires FROM UserApp WHERE ResetToken = ? LIMIT 1');
        $stmt->execute([$resetToken]);
        $row = $stmt->fetch();
        if (!$row) {
            http_response_code(400);
            echo json_encode(['success' => false, 'message' => 'Invalid reset token'], JSON_UNESCAPED_UNICODE);
            break;
        }
        if (strtotime($row['ResetExpires']) < time()) {
            http_response_code(400);
            echo json_encode(['success' => false, 'message' => 'Reset token expired'], JSON_UNESCAPED_UNICODE);
            break;
        }

        $hash = password_hash($newPassword, PASSWORD_DEFAULT);
        $stmt = $pdo->prepare('UPDATE UserApp SET Password = ?, ResetToken = NULL, ResetExpires = NULL WHERE ID = ?');
        $stmt->execute([$hash, $row['ID']]);

        echo json_encode(['success' => true, 'message' => 'Password updated'], JSON_UNESCAPED_UNICODE);
        break;

    case 'profile_get':
        requireAuth();
        getProfile($pdo);
        break;

    case 'profile_update':
        requireAuth();
        $body = jsonInput();
        updateProfile($pdo, $body);
        break;

    default:
        http_response_code(400);
        echo json_encode(['success' => false, 'error' => 'Invalid or missing action. Use: products, products_by_category, register, login, logout, profile_get, profile_update'], JSON_UNESCAPED_UNICODE);
        break;
}

// ----------------- IMPLEMENTATION -----------------

function getProducts(PDO $pdo, $category = null) {
    if ($category) {
        $sql = "SELECT ID, ProductName, Category, ImageURL FROM Product WHERE Category = :cat ORDER BY ID";
        $stmt = $pdo->prepare($sql);
        $stmt->execute([':cat' => $category]);
    } else {
        $sql = "SELECT ID, ProductName, Category, ImageURL FROM Product ORDER BY ID";
        $stmt = $pdo->query($sql);
    }
    $rows = $stmt->fetchAll();
    echo json_encode(['success' => true, 'count' => count($rows), 'data' => $rows], JSON_UNESCAPED_UNICODE);
}

function registerUser(PDO $pdo, array $body) {
    $account = trim($body['account'] ?? '');
    $password = $body['password'] ?? '';
    if ($account === '' || $password === '') {
        http_response_code(400);
        echo json_encode(['success' => false, 'error' => 'account and password required'], JSON_UNESCAPED_UNICODE);
        return;
    }

    // check existing account
    $stmt = $pdo->prepare('SELECT ID FROM UserApp WHERE Account = ? LIMIT 1');
    $stmt->execute([$account]);
    if ($stmt->fetch()) {
        http_response_code(400);
        echo json_encode(['success' => false, 'error' => 'Account already exists'], JSON_UNESCAPED_UNICODE);
        return;
    }

    $hash = password_hash($password, PASSWORD_DEFAULT);
    $stmt = $pdo->prepare('INSERT INTO UserApp (Account, Password) VALUES (?, ?)');
    $stmt->execute([$account, $hash]);
    $userId = $pdo->lastInsertId();

    // create corresponding UserDB row
    $stmt = $pdo->prepare('INSERT INTO UserDB (Account, FirstName, LastName, ImageURL) VALUES (?, NULL, NULL, NULL)');
    $stmt->execute([$account]);

    // auto-login (session)
    $_SESSION['account'] = $account;

    echo json_encode(['success' => true, 'data' => ['id' => (int)$userId, 'account' => $account]], JSON_UNESCAPED_UNICODE);
}

function loginUser(PDO $pdo, array $body) {
    $account = trim($body['account'] ?? '');
    $password = $body['password'] ?? '';
    if ($account === '' || $password === '') {
        http_response_code(400);
        echo json_encode(['success' => false, 'error' => 'account and password required'], JSON_UNESCAPED_UNICODE);
        return;
    }

    $stmt = $pdo->prepare('SELECT ID, Password FROM UserApp WHERE Account = ? LIMIT 1');
    $stmt->execute([$account]);
    $row = $stmt->fetch();
    if (!$row) {
        http_response_code(401);
        echo json_encode(['success' => false, 'error' => 'Invalid credentials'], JSON_UNESCAPED_UNICODE);
        return;
    }

    if (!password_verify($password, $row['Password'])) {
        http_response_code(401);
        echo json_encode(['success' => false, 'error' => 'Invalid credentials'], JSON_UNESCAPED_UNICODE);
        return;
    }

    // set session
    $_SESSION['account'] = $account;

    // fetch profile
    $stmt = $pdo->prepare('SELECT ID AS ProfileID, Account, FirstName, LastName, ImageURL FROM UserDB WHERE Account = ? LIMIT 1');
    $stmt->execute([$account]);
    $profile = $stmt->fetch();

    echo json_encode(['success' => true, 'data' => ['account' => $account, 'profile' => $profile]], JSON_UNESCAPED_UNICODE);
}

function logoutUser() {
    session_unset();
    session_destroy();
    setcookie(session_name(), '', time() - 3600, '/');
    echo json_encode(['success' => true], JSON_UNESCAPED_UNICODE);
}

function getProfile(PDO $pdo) {
    $account = $_SESSION['account'];
    $stmt = $pdo->prepare('SELECT ID AS ProfileID, Account, FirstName, LastName, ImageURL FROM UserDB WHERE Account = ? LIMIT 1');
    $stmt->execute([$account]);
    $profile = $stmt->fetch();
    if (!$profile) {
        http_response_code(404);
        echo json_encode(['success' => false, 'error' => 'Profile not found'], JSON_UNESCAPED_UNICODE);
        return;
    }
    echo json_encode(['success' => true, 'data' => $profile], JSON_UNESCAPED_UNICODE);
}

function updateProfile(PDO $pdo, array $body) {
    $account = $_SESSION['account'];
    $firstName = array_key_exists('firstName', $body) ? $body['firstName'] : null;
    $lastName = array_key_exists('lastName', $body) ? $body['lastName'] : null;
    $imageURL = array_key_exists('imageURL', $body) ? $body['imageURL'] : null;
    $password = array_key_exists('password', $body) ? $body['password'] : null;

    if ($firstName === null && $lastName === null && $imageURL === null && $password === null) {
        http_response_code(400);
        echo json_encode(['success' => false, 'error' => 'Nothing to update'], JSON_UNESCAPED_UNICODE);
        return;
    }

    $pdo->beginTransaction();
    try {
        $updates = [];
        $params = [];

        if ($firstName !== null) { $updates[] = 'FirstName = ?'; $params[] = $firstName; }
        if ($lastName !== null) { $updates[] = 'LastName = ?'; $params[] = $lastName; }
        if ($imageURL !== null) { $updates[] = 'ImageURL = ?'; $params[] = $imageURL; }

        if (count($updates) > 0) {
            $params[] = $account;
            $sql = 'UPDATE UserDB SET ' . implode(', ', $updates) . ' WHERE Account = ?';
            $stmt = $pdo->prepare($sql);
            $stmt->execute($params);
        }

        if ($password !== null && $password !== '') {
            $hash = password_hash($password, PASSWORD_DEFAULT);
            $stmt = $pdo->prepare('UPDATE UserApp SET Password = ? WHERE Account = ?');
            $stmt->execute([$hash, $account]);
        }

        $pdo->commit();
    } catch (\Exception $e) {
        $pdo->rollBack();
        http_response_code(500);
        echo json_encode(['success' => false, 'error' => 'DB error', 'message' => $e->getMessage()], JSON_UNESCAPED_UNICODE);
        return;
    }

    // return updated profile
    $stmt = $pdo->prepare('SELECT ID AS ProfileID, Account, FirstName, LastName, ImageURL FROM UserDB WHERE Account = ? LIMIT 1');
    $stmt->execute([$account]);
    $profile = $stmt->fetch();
    echo json_encode(['success' => true, 'data' => $profile], JSON_UNESCAPED_UNICODE);
}
