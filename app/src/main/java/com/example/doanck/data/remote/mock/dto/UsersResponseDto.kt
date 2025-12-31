package com.example.doanck.data.remote.mock.dto

data class UsersResponseDto(val users: List<UserDto>)

data class UserDto(
    val id: Int,
    val firstName: String,
    val lastName: String,
    val address: AddressDto
)

data class AddressDto(val address: String)
