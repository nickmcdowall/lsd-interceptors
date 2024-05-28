package io.lsdconsulting.intercceptors.example.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id

@Entity
data class Fish(
    @Id
    var id: Long?,

    @Column
    var name: String?,
) {

    override fun toString(): String {
        return "Fish(id=$id, name=$name)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Fish

        if (id != other.id) return false
        return name == other.name
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + (name?.hashCode() ?: 0)
        return result
    }
}
