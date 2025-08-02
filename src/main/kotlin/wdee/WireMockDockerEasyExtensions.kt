package wdee

import wdee.handler.CommandHandler

object WireMockDockerEasyExtensions {
    @JvmStatic
    fun main(args: Array<String>) {
        CommandHandler().run(args)
    }
}
