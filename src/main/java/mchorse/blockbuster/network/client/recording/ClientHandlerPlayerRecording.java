package mchorse.blockbuster.network.client.recording;

import mchorse.blockbuster.capabilities.morphing.Morphing;
import mchorse.blockbuster.common.ClientProxy;
import mchorse.blockbuster.network.Dispatcher;
import mchorse.blockbuster.network.client.ClientMessageHandler;
import mchorse.blockbuster.network.common.recording.PacketFramesSave;
import mchorse.blockbuster.network.common.recording.PacketPlayerRecording;
import mchorse.blockbuster.recording.data.Mode;
import mchorse.blockbuster.recording.data.Record;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Client hanlder player recording
 *
 * This client handler is responsible for updating recording overlay status and
 * starting or stopping the recording based on the state given from packet.
 */
public class ClientHandlerPlayerRecording extends ClientMessageHandler<PacketPlayerRecording>
{
    @Override
    @SideOnly(Side.CLIENT)
    public void run(EntityPlayerSP player, PacketPlayerRecording message)
    {
        ClientProxy.recordingOverlay.setVisible(message.recording);
        ClientProxy.recordingOverlay.setCaption(message.filename);

        if (message.recording)
        {
            ClientProxy.manager.startRecording(message.filename, player, Mode.FRAMES, false);
        }
        else
        {
            Morphing.get(player).reset();

            Record record = ClientProxy.manager.recorders.get(player).record;

            Dispatcher.sendToServer(new PacketFramesSave(record.filename, record.frames));
            ClientProxy.manager.stopRecording(player, false);
        }
    }
}