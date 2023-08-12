package io.lsdconsulting.interceptors.common

import org.slf4j.Logger
import org.slf4j.LoggerFactory

inline fun <reified T> T.log(): Logger = LoggerFactory.getLogger(T::class.java)

fun log(): Logger = LoggerFactory.getLogger(Thread.currentThread().stackTrace[2].methodName)