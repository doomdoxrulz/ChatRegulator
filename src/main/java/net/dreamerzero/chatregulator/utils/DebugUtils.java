package net.dreamerzero.chatregulator.utils;

import org.slf4j.Logger;

import de.leonhard.storage.Yaml;
import net.dreamerzero.chatregulator.InfractionPlayer;
import net.dreamerzero.chatregulator.modules.checks.AbstractCheck;
import net.dreamerzero.chatregulator.modules.checks.SpamCheck;
import net.dreamerzero.chatregulator.utils.TypeUtils.InfractionType;

/**
 * Utilities for bug or inconsistency resolution
 */
public class DebugUtils {
    private Logger logger;
    private Yaml config;

    /**
     * Creates a debug object for easier troubleshooting
     * @param logger the logger
     * @param config the plugin config
     */
    public DebugUtils(Logger logger, Yaml config){
        this.logger = logger;
        this.config = config;
    }

    /**
     * Debug message
     * @param player the {@link InfractionPlayer} involved
     * @param string the message/command
     * @param detection the detection type
     * @param check the check
     */
    public void debug(InfractionPlayer player, String string, InfractionType detection, AbstractCheck check){
        final String pattern = check instanceof SpamCheck ? check.getInfractionWord() : check.getPattern();

        if (config.getBoolean("debug")){
            logger.info("User Detected: {}", player.getPlayer().get().getUsername());
            logger.info("Detection: {}", detection.toString());
            logger.info("String: {}", string);
            logger.info("Pattern: {}", pattern);
        }
    }
}
