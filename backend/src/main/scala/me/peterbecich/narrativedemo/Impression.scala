package me.peterbecich.narrativedemo

import java.util.UUID
import java.time.LocalDateTime

case class Impression(impressionId: UUID, timestamp: LocalDateTime, userId: UUID)
