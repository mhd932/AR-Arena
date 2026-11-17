package com.example.ararena


data class V3(val x: Float, val y: Float, val z: Float) {
    operator fun minus(o: V3) = V3(x - o.x, y - o.y, z - o.z)
    fun dot(o: V3) = x * o.x + y * o.y + z * o.z
}

class Player(val id: String, var name: String) {
    var pos = V3(0f, 0f, 0f)   // dans le repère commun (origine calibrée)
    var hp = 100
    var kills = 0
    var deaths = 0
    val alive get() = hp > 0
}

object GameLogic {
    const val HIT_RADIUS = 0.35f   // "corps" d'un joueur : sphère de 35 cm
    const val DAMAGE = 25
    const val RANGE = 15f

    /** Tir hitscan : rayon (origin, dir normalisé) contre sphères des adversaires. */
    fun findTarget(origin: V3, dir: V3, others: Collection<Player>): Player? =
        others.filter { it.alive }.mapNotNull { p ->
            val oc = p.pos - origin
            val t = oc.dot(dir)
            if (t < 0f || t > RANGE) return@mapNotNull null
            val d2 = oc.dot(oc) - t * t
            if (d2 <= HIT_RADIUS * HIT_RADIUS) p to t else null
        }.minByOrNull { it.second }?.first

    /** Applique un tir ; renvoie true si la cible meurt. */
    fun applyHit(shooter: Player, target: Player): Boolean {
        if (!target.alive) return false
        target.hp = (target.hp - DAMAGE).coerceAtLeast(0)
        if (target.hp <= 0) { shooter.kills++; target.deaths++; return true }
        return false
    }

    fun respawn(p: Player) { p.hp = 100 }
}
