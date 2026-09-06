package com.wifite.security.wifite2

class ResultParser {
    fun parseAttackResult(output: String): Map<String, String> {
        return mapOf(
            "status" to if (output.contains("KEY FOUND")) "SUCCESS" else "FAILED",
            "password" to extractPassword(output),
            "time" to extractTime(output)
        )
    }
    
    private fun extractPassword(output: String): String {
        return output.lines().find { it.contains("KEY") }?.split(":")?.last() ?: ""
    }
    
    private fun extractTime(output: String): String {
        return output.lines().find { it.contains("time") }?.split(":")?.last() ?: ""
    }
}
