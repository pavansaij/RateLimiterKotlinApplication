package SamplePackage.Models

import java.lang.IllegalArgumentException

enum class Type {
    open, close;

    companion object {
        fun getCaseInsensitiveType(inpType: String): Type {
            if (Type.values().any { it.name == inpType.toLowerCase() }) {
                return Type.valueOf(inpType.toLowerCase())
            } else {
                throw IllegalArgumentException("Invalid type string passed in input")
            }
        }
    }
}