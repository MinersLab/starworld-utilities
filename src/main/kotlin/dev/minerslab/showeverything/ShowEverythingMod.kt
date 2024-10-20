package dev.minerslab.showeverything

import net.fabricmc.api.ModInitializer
import net.minecraft.util.Identifier
import org.slf4j.LoggerFactory

object ShowEverythingMod : ModInitializer {

	const val ID = "showeverything"

    val LOGGER = LoggerFactory.getLogger(ID)

	override fun onInitialize() {
        CommonProxy.initialize()
		LOGGER.info("Initialized!")
	}

	fun rl(path: String) = Identifier.of(ID, path)
	fun rls(path: String) = rl(path).toString()

}