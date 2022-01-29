package me.dreamerzero.chatregulator.modules.checks;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import org.jetbrains.annotations.NotNull;

import me.dreamerzero.chatregulator.config.Configuration;
import me.dreamerzero.chatregulator.enums.InfractionType;
import me.dreamerzero.chatregulator.result.ReplaceableResult;
import me.dreamerzero.chatregulator.result.Result;

/**
 * Check for invalid characters
 */
public class UnicodeCheck extends AbstractCheck {
    private final List<Character> results;

    public UnicodeCheck(){
        results = new ArrayList<>();
    }

    @Override
    public CompletableFuture<? extends Result> check(@NotNull String message) {
        char[] charArray = Objects.requireNonNull(message).toCharArray();
        boolean blockable = Configuration.getConfig().getUnicodeConfig().isBlockable();

        for(char character : charArray){
            if(!((character > '\u0020' && character < '\u007E') || (character < '\u00FC' && character < '\u00BF') || (character > '\u00BF' && character < '\u00FE'))){
                if(blockable){
                    return CompletableFuture.completedFuture(new Result(message, true));
                }
                results.add(character);
            }
        }

        if(!results.isEmpty()){
            super.string = results.toString();
            return CompletableFuture.completedFuture(new ReplaceableResult(message, detected){
                @Override
                public String replaceInfraction(){
                    String replaced = message;
                    for(var character : results){
                        replaced = replaced.replace(character, ' ');
                    }
                    return replaced;
                }
            });
        }
        return CompletableFuture.completedFuture(new Result(message, false));

    }

    @Override
    public @NotNull InfractionType type() {
        return InfractionType.UNICODE;
    }
}
