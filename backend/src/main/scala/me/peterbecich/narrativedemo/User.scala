package me.peterbecich.narrativedemo

import java.util.UUID

/*
[error]   If me.peterbecich.narrativedemo.User is a simple type (or option thereof) that maps to a single column, you're
[error]   probably missing a Meta instance. If me.peterbecich.narrativedemo.User is a product type (typically a case class,

https://static.javadoc.io/org.tpolecat/doobie-core_2.12/0.5.0-M13/doobie/util/meta$.html#Meta[A]extendsAnyRef

http://tpolecat.github.io/doobie/docs/17-FAQ.html#how-do-i-resolve-error-could-not-find-or-construct-param
 */


case class User(userId: UUID)
