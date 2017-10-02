package com.ubiqsmart.extensions

import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.primaryConstructor

infix inline fun <reified T : Any> T.merge(other: T): T {
  val nameToProperty = T::class.declaredMemberProperties.associateBy { it.name }
  val primaryConstructor = T::class.primaryConstructor!!
  val args = primaryConstructor.parameters.associate { parameter ->
    val property = nameToProperty[parameter.name]!!
    parameter to (property.get(this) ?: property.get(other))
  }
  return primaryConstructor.callBy(args)
}