package mchorse.blockbuster.client.gui.dashboard.panels.recording_editor.actions;

import mchorse.blockbuster.client.gui.dashboard.panels.recording_editor.GuiRecordingEditorPanel;
import mchorse.blockbuster.recording.actions.PlaceBlockAction;
import mchorse.mclib.client.gui.framework.elements.input.GuiTextElement;
import mchorse.mclib.client.gui.framework.elements.input.GuiTrackpadElement;
import mchorse.mclib.client.gui.utils.keys.IKey;
import net.minecraft.client.Minecraft;

public class GuiPlaceBlockActionPanel extends GuiBlockActionPanel<PlaceBlockAction>
{
    public GuiTextElement block;
    public GuiTrackpadElement meta;

    public GuiPlaceBlockActionPanel(Minecraft mc, GuiRecordingEditorPanel panel)
    {
        super(mc, panel);

        this.block = new GuiTextElement(mc, (str) -> this.action.block = str);
        this.meta = new GuiTrackpadElement(mc, (value) -> this.action.metadata = value.byteValue());
        this.meta.tooltip(IKey.lang("blockbuster.gui.record_editor.meta"));

        this.block.flex().set(0, -30, 100, 20).relative(this.meta.resizer());
        this.meta.flex().set(0, -30, 100, 20).relative(this.x.resizer());
        this.meta.limit(0, 15, true);

        this.add(this.block, this.meta);
    }

    @Override
    public void fill(PlaceBlockAction action)
    {
        super.fill(action);

        this.block.setText(action.block);
        this.meta.setValue(action.metadata);
    }
}