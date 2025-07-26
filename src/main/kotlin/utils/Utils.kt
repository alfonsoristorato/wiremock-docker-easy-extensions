package utils

object Utils {
    fun isOsWindows() = System.getProperty("os.name").lowercase().contains("windows")
}
