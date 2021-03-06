package mchorse.blockbuster.aperture.gui;

import mchorse.aperture.ClientProxy;
import mchorse.aperture.client.gui.GuiCameraEditor;
import mchorse.aperture.client.gui.config.GuiAbstractConfigOptions;
import mchorse.blockbuster.aperture.CameraHandler;
import mchorse.blockbuster.network.Dispatcher;
import mchorse.blockbuster.network.common.scene.sync.PacketScenePlay;
import mchorse.blockbuster.recording.scene.SceneLocation;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiButtonElement;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiToggleElement;
import mchorse.mclib.client.gui.utils.keys.IKey;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiDirectorConfigOptions extends GuiAbstractConfigOptions
{
    public GuiButtonElement detachScene;
    public GuiToggleElement actions;
    public GuiToggleElement reload;
    public GuiButtonElement reloadScene;

    public GuiDirectorConfigOptions(Minecraft mc, GuiCameraEditor editor)
    {
        super(mc, editor);

        this.detachScene = new GuiButtonElement(mc, IKey.lang("blockbuster.gui.aperture.config.detach"), (b) ->
        {
            if (CameraHandler.location != null)
            {
                Dispatcher.sendToServer(new PacketScenePlay(CameraHandler.location, PacketScenePlay.STOP, 0));

                CameraHandler.location = null;
                b.setEnabled(false);
            }
        });

        this.reload = new GuiToggleElement(mc, IKey.lang("blockbuster.gui.aperture.config.reload"), CameraHandler.reload, (b) ->
        {
            CameraHandler.reload = this.reload.isToggled();
        });

        this.actions = new GuiToggleElement(mc, IKey.lang("blockbuster.gui.aperture.config.actions"), CameraHandler.actions, (b) ->
        {
            CameraHandler.actions = this.actions.isToggled();
        });

        this.reloadScene = new GuiButtonElement(mc, IKey.lang("blockbuster.gui.aperture.config.reload_scene"), (b) ->
        {
            SceneLocation location = CameraHandler.get();

            if (location != null)
            {
                Dispatcher.sendToServer(new PacketScenePlay(location, PacketScenePlay.RESTART, ClientProxy.getCameraEditor().timeline.value));
            }
        });

        this.add(this.detachScene, this.reload, this.actions, this.reloadScene);
    }

    @Override
    public IKey getTitle()
    {
        return IKey.lang("blockbuster.gui.aperture.config.title");
    }

    @Override
    public void update()
    {
        this.reload.toggled(CameraHandler.reload);
        this.actions.toggled(CameraHandler.actions);
    }

    @Override
    public void resize()
    {
        super.resize();

        this.detachScene.setEnabled(CameraHandler.location != null);
    }
}