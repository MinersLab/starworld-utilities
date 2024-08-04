package starworld.core

import net.fabricmc.api.ModInitializer
import org.slf4j.LoggerFactory

object StarWorldCoreLib : ModInitializer {

	const val ID = "starworldcorelib"

    val LOGGER = LoggerFactory.getLogger(ID)

	override fun onInitialize() {
		CommonProxy.initialize()
		LOGGER.info("Initialized!")
	}

}