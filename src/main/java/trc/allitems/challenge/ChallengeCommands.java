package trc.allitems.challenge;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.server.level.ServerPlayer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import trc.allitems.network.ChallengeSync;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import org.jetbrains.annotations.NotNull;

public final class ChallengeCommands {
    private static final int CONFIRM_WINDOW_TICKS = 20 * 30;
    private static final Map<UUID, Integer> PENDING_RESETS = new HashMap<>();

    private ChallengeCommands() {}

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, _, _) -> build(dispatcher));
    }

    private static void build(@NotNull CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("allitems")
                .requires(CommandSourceStack::isPlayer)
                .then(Commands.literal("start")
                        .executes(ctx -> run(ctx, player -> ChallengeManager.start(player, ChallengeMode.FREE)))
                        .then(Commands.literal("order")
                                .executes(ctx -> run(ctx, player -> ChallengeManager.start(player, ChallengeMode.ORDER)))))
                .then(Commands.literal("pause").executes(ctx -> run(ctx, ChallengeManager::pause)))
                .then(Commands.literal("resume").executes(ctx -> run(ctx, ChallengeManager::resume)))
                .then(Commands.literal("skip")
                        .executes(ctx -> run(ctx, ChallengeManager::skip))
                        .then(Commands.argument("item", IdentifierArgument.id())
                                .suggests((ctx, builder) -> {
                                    ServerPlayer player = ctx.getSource().getPlayer();
                                    return SharedSuggestionProvider.suggestResource(
                                            player == null ? List.of() : ChallengeManager.skippable(player), builder);
                                })
                                .executes(ctx -> run(ctx, player -> ChallengeManager.skipItem(player, IdentifierArgument.getId(ctx, "item"))))))
                .then(Commands.literal("reset")
                        .executes(ChallengeCommands::requestReset)
                        .then(Commands.literal("confirm").executes(ChallengeCommands::confirmReset)))
                .then(Commands.literal("list").executes(ctx -> {
                    ChallengeSync.list(ctx.getSource().getPlayerOrException());
                    return 1;
                })));
    }

    private static int run(@NotNull CommandContext<CommandSourceStack> ctx,
                           @NotNull Function<ServerPlayer, ChallengeManager.Result> action) throws CommandSyntaxException {
        ChallengeManager.Result result = action.apply(ctx.getSource().getPlayerOrException());

        if (!result.success()) {
            ctx.getSource().sendFailure(Component.literal(result.message()));
            return 0;
        }

        if (result.message() != null) ctx.getSource().sendSuccess(() -> Component.literal(result.message()), false);
        return 1;
    }

    private static int requestReset(@NotNull CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();

        if (!ChallengeManager.hasChallenge(player)) {
            ctx.getSource().sendFailure(Component.literal("No challenge started."));
            return 0;
        }

        PENDING_RESETS.put(player.getUUID(), ctx.getSource().getServer().getTickCount());

        Component confirm = Component.literal("[Confirm reset]").withStyle(style -> style
                .withColor(ChatFormatting.RED)
                .withBold(true)
                .withClickEvent(new ClickEvent.RunCommand("/allitems reset confirm"))
                .withHoverEvent(new HoverEvent.ShowText(Component.literal("/allitems reset confirm"))));

        ctx.getSource().sendSuccess(() -> Component.literal("This deletes all progress of your challenge. Click ")
                .withStyle(ChatFormatting.YELLOW)
                .append(confirm)
                .append(Component.literal(" or run /allitems reset confirm within 30 seconds.").withStyle(ChatFormatting.YELLOW)), false);
        return 1;
    }

    private static int confirmReset(@NotNull CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        Integer requested = PENDING_RESETS.remove(player.getUUID());

        if (requested == null || ctx.getSource().getServer().getTickCount() - requested > CONFIRM_WINDOW_TICKS) {
            ctx.getSource().sendFailure(Component.literal("Nothing to confirm. Run /allitems reset first."));
            return 0;
        }

        return run(ctx, ChallengeManager::reset);
    }
}