package no.fintlabs.dynamiskadapter.util

fun freePort(port: Int) {
    try {
        val find = ProcessBuilder("lsof", "-ti", "tcp:$port")
            .redirectErrorStream(true)
            .start()

        val pids = find.inputStream.bufferedReader().readLines()

        if (pids.isEmpty()) {
            println("Port $port is free.")
            return
        }

        for (pid in pids) {
            println("Killing process using port $port: PID=$pid")
            Runtime.getRuntime().exec("kill -9 $pid")
        }

        Thread.sleep(200)

    } catch (e: Exception) {
        println("Failed to inspect or free port $port: ${e.message}")
    }
}