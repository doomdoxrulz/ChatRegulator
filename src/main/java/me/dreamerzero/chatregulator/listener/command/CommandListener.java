package me.dreamerzero.chatregulator.listener.command;

import com.velocitypowered.api.event.Continuation;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.command.CommandExecuteEvent;
import com.velocitypowered.api.proxy.Player;

import org.jetbrains.annotations.ApiStatus.Internal;

import me.dreamerzero.chatregulator.ChatRegulator;
import me.dreamerzero.chatregulator.InfractionPlayer;
import me.dreamerzero.chatregulator.modules.checks.CommandCheck;
import me.dreamerzero.chatregulator.modules.checks.SyntaxCheck;
import me.dreamerzero.chatregulator.result.Result;
import me.dreamerzero.chatregulator.utils.CommandUtils;
import me.dreamerzero.chatregulator.utils.GeneralUtils.EventBundle;
import me.dreamerzero.chatregulator.wrapper.event.CommandWrapper;
import me.dreamerzero.chatregulator.wrapper.event.EventWrapper;
import me.dreamerzero.chatregulator.enums.InfractionType;
import me.dreamerzero.chatregulator.enums.SourceType;

import static me.dreamerzero.chatregulator.utils.GeneralUtils.*;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Detections related to command execution by players
 */
@Internal
public final class CommandListener {
    private final ChatRegulator plugin;
    public CommandListener(ChatRegulator plugin){
        this.plugin = plugin;
    }
    /**
     * Listener for command detections
     * @param event the command event
     * @param continuation the event cycle
     */
    @Subscribe(order = PostOrder.FIRST)
    public void onCommand(CommandExecuteEvent event, Continuation continuation){
        if (!(event.getCommandSource() instanceof final Player player)
            || !event.getResult().isAllowed()
        ) {
            continuation.resume();
            return;
        }

        final InfractionPlayer infractionPlayer = InfractionPlayer.get(player);
        final EventWrapper<CommandExecuteEvent> wrapper = new CommandWrapper(event, continuation);

        if(this.blockedCommands(infractionPlayer, event.getCommand(), wrapper)
            || this.syntax(infractionPlayer, event.getCommand(), wrapper)
            || this.checkIfCanCheck(event.getCommand(), wrapper)
        ) {
            return;
        }

        final AtomicReference<String> command = new AtomicReference<>(event.getCommand());

        if(unicode(infractionPlayer, command, wrapper, plugin)
            || caps(infractionPlayer, command, wrapper, plugin)
            || flood(infractionPlayer, command, wrapper, plugin)
            || regular(infractionPlayer, command, wrapper, plugin)
            || spam(infractionPlayer, command, wrapper, plugin)
        ) {
            return;
        }

        infractionPlayer.lastCommand(command.get());
        continuation.resume();
    }

    private boolean checkIfCanCheck(final String command, EventWrapper<?> wrapper) {
        for(final String cmd : plugin.getConfig().getCommandsChecked()){
            if(CommandUtils.isStartingString(command, cmd))
                return false;
        }
        wrapper.resume();
        return true;
    }

    private boolean syntax(InfractionPlayer player, String string, EventWrapper<?> event) {
        if(allowedPlayer(player.getPlayer(), InfractionType.SYNTAX, plugin.getConfig())
            && checkAndCall(
                new EventBundle(
                    player,
                    string,
                    InfractionType.SYNTAX,
                    SyntaxCheck.createCheck(string).exceptionallyAsync(e -> {
                        plugin.getLogger().error("An Error ocurred on Syntax Check", e);
                        return new Result("", false);
                    }).join(),
                    SourceType.COMMAND
                ),
                plugin
            )
        ) {
            if (!event.canBeModified()) {
                return false;
            }
            event.cancel();
            event.resume();
            return true;
        }
        return false;
    }

    private boolean blockedCommands(InfractionPlayer player, String string, EventWrapper<?> event) {
        if(allowedPlayer(player.getPlayer(), InfractionType.BCOMMAND, plugin.getConfig())
            && checkAndCall(
                new EventBundle(
                    player,
                    string,
                    InfractionType.BCOMMAND,
                    CommandCheck.createCheck(string, plugin.getBlacklist()).exceptionallyAsync(e -> {
                        plugin.getLogger().error("An Error ocurred on Blocked Commands Check", e);
                        return new Result("", false);
                    }).join(),
                    SourceType.COMMAND
                ),
                plugin
            )
        ) {
            if (!event.canBeModified()) {
                return false;
            }
            event.cancel();
            event.resume();
            return true;
        }
        return false;
    }
}
