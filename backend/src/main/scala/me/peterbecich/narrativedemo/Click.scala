package me.peterbecich.narrativedemo

import java.util.UUID
import java.time.LocalDateTime

case class Click(clickId: UUID, timestamp: LocalDateTime, userId: UUID)
