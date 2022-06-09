package com.spin.id.api.response.topic

data class TopicData(
    val description: String = "",
    val id: String = "",
    val image: String = "",
    val name: String = "",
    val codename: String = "",
    val sort_order: String = "",
    val status: String = "",
    var isSelected: Boolean = false

) {
    override fun equals(other: Any?): Boolean {
        if (other === this) return true

        if (other !is TopicData) {
            return false
        }

        val data: TopicData = other

        return !isSelected && data.id.toInt() == id.toInt()
    }

    override fun hashCode(): Int {
        return id.toInt()
    }
}