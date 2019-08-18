package ru.serega6531.mafia.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ru.serega6531.mafia.AuthData;
import ru.serega6531.mafia.packets.MafiaPacket;
import ru.serega6531.mafia.packets.server.LobbyJoinedPacket;
import ru.serega6531.mafia.packets.server.LobbyUpdatedPacket;
import ru.serega6531.mafia.packets.server.LoginResponsePacket;

public class MafiaClientHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        final MafiaPacket packet = (MafiaPacket) msg;
        System.out.println(packet);

        if(packet instanceof LoginResponsePacket) {
            LoginResponsePacket response = ((LoginResponsePacket) packet);
            final AuthData authData = response.getAuthData();
            MafiaClient.setAuthData(authData);
            MafiaClient.getObservableLobbiesList().addAll(response.getLobbies());

            Parent root = FXMLLoader.load(getClass().getResource("/lobbies.fxml"));
            final Stage primaryStage = MafiaClient.getPrimaryStage();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/lobbies.css").toExternalForm());
            MafiaClient.setLobbiesListScene(scene);

            Platform.runLater(() -> primaryStage.setScene(scene));
        } else if(packet instanceof LobbyJoinedPacket) {
//            ctx.channel().writeAndFlush(new StartSessionPacket("Test", new byte[0]));
        } else if(packet instanceof LobbyUpdatedPacket) {

        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.channel().close();
    }
}
