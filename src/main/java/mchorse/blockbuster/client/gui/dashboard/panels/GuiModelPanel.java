package mchorse.blockbuster.client.gui.dashboard.panels;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import mchorse.blockbuster.client.gui.elements.GuiMorphsPopup;
import mchorse.blockbuster.client.gui.framework.elements.GuiButtonElement;
import mchorse.blockbuster.client.gui.framework.elements.GuiElement;
import mchorse.blockbuster.client.gui.framework.elements.GuiTrackpadElement;
import mchorse.blockbuster.client.gui.framework.elements.IGuiLegacy;
import mchorse.blockbuster.client.gui.utils.Area;
import mchorse.blockbuster.client.gui.utils.Resizer.Measure;
import mchorse.blockbuster.client.gui.widgets.GuiInventory;
import mchorse.blockbuster.client.gui.widgets.GuiInventory.IInventoryPicker;
import mchorse.blockbuster.client.gui.widgets.GuiSlot;
import mchorse.blockbuster.client.gui.widgets.buttons.GuiCirculate;
import mchorse.blockbuster.common.tileentity.TileEntityModel;
import mchorse.blockbuster.common.tileentity.TileEntityModel.RotationOrder;
import mchorse.blockbuster.network.Dispatcher;
import mchorse.blockbuster.network.common.PacketModifyModelBlock;
import mchorse.metamorph.capabilities.morphing.Morphing;
import mchorse.metamorph.client.gui.elements.GuiCreativeMorphs.MorphCell;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.client.config.GuiCheckBox;

public class GuiModelPanel extends GuiDashboardPanel implements IGuiLegacy, IInventoryPicker
{
    public static final List<BlockPos> lastBlocks = new ArrayList<BlockPos>();

    private TileEntityModel model;
    private TileEntityModel temp = new TileEntityModel();

    private GuiMorphsPopup morphs;

    private GuiTrackpadElement yaw;
    private GuiTrackpadElement pitch;
    private GuiTrackpadElement body;

    private GuiTrackpadElement x;
    private GuiTrackpadElement y;
    private GuiTrackpadElement z;

    private GuiTrackpadElement sx;
    private GuiTrackpadElement sy;
    private GuiTrackpadElement sz;

    private GuiTrackpadElement rx;
    private GuiTrackpadElement ry;
    private GuiTrackpadElement rz;

    private GuiButtonElement<GuiCheckBox> one;
    private GuiButtonElement<GuiCirculate> order;

    private GuiInventory inventory;
    private GuiSlot[] slots = new GuiSlot[6];
    private GuiSlot active;

    public GuiModelPanel(Minecraft mc)
    {
        super(mc);

        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        GuiElement element = null;

        this.morphs = new GuiMorphsPopup(6, null, Morphing.get(player));

        /* Entity angles */
        this.children.add(this.yaw = new GuiTrackpadElement(mc, "Yaw", (value) -> this.model.rotateYawHead = value));
        this.yaw.resizer().set(10, 20, 80, 20).parent(this.area);

        this.children.add(this.pitch = new GuiTrackpadElement(mc, "Pitch", (value) -> this.model.rotatePitch = value));
        this.pitch.resizer().set(0, 25, 80, 20).relative(this.yaw.resizer);

        this.children.add(this.body = new GuiTrackpadElement(mc, "Body", (value) -> this.model.rotateBody = value));
        this.body.resizer().set(0, 25, 80, 20).relative(this.pitch.resizer);

        /* Rotation */
        this.children.add(this.rx = new GuiTrackpadElement(mc, "X", (value) -> this.model.rx = value));
        this.rx.resizer().set(0, 45, 80, 20).relative(this.body.resizer);

        this.children.add(this.ry = new GuiTrackpadElement(mc, "Y", (value) -> this.model.ry = value));
        this.ry.resizer().set(0, 25, 80, 20).relative(this.rx.resizer);

        this.children.add(this.rz = new GuiTrackpadElement(mc, "Z", (value) -> this.model.rz = value));
        this.rz.resizer().set(0, 25, 80, 20).relative(this.ry.resizer);

        /* Translation */
        this.children.add(this.x = new GuiTrackpadElement(mc, "X", (value) -> this.model.x = value));
        this.x.resizer().set(0, 20, 80, 20).parent(this.area).x.set(1, Measure.RELATIVE, -90);

        this.children.add(this.y = new GuiTrackpadElement(mc, "Y", (value) -> this.model.y = value));
        this.y.resizer().set(0, 25, 80, 20).relative(this.x.resizer);

        this.children.add(this.z = new GuiTrackpadElement(mc, "Z", (value) -> this.model.z = value));
        this.z.resizer().set(0, 25, 80, 20).relative(this.y.resizer);

        /* Scale */
        this.children.add(this.sx = new GuiTrackpadElement(mc, "X", (value) -> this.model.sx = value));
        this.sx.resizer().set(0, 45, 80, 20).relative(this.z.resizer);

        this.children.add(this.sy = new GuiTrackpadElement(mc, "Y", (value) -> this.model.sy = value));
        this.sy.resizer().set(0, 25, 80, 20).relative(this.sx.resizer);

        this.children.add(this.sz = new GuiTrackpadElement(mc, "Z", (value) -> this.model.sz = value));
        this.sz.resizer().set(0, 25, 80, 20).relative(this.sy.resizer);

        /* Buttons */
        this.children.add(element = GuiButtonElement.button(mc, "Pick morph", (button) -> this.morphs.morphs.setHidden(false)));
        element.resizer().set(0, 10, 70, 20).parent(this.area).x.set(0.5F, Measure.RELATIVE, -35);

        this.children.add(this.one = GuiButtonElement.checkbox(mc, "One", false, (button) -> this.toggleOne()));
        this.one.resizer().set(50, -14, 30, 11).relative(this.sx.resizer);

        GuiCirculate button = new GuiCirculate(0, 0, 0, 0, 0);
        button.addLabel("ZYX");
        button.addLabel("XYZ");

        this.children.add(this.order = new GuiButtonElement<GuiCirculate>(mc, button, (b) -> this.model.order = RotationOrder.values()[b.button.getValue()]));
        this.order.resizer().set(40, -22, 40, 20).relative(this.rx.resizer);

        /* Inventory */
        this.inventory = new GuiInventory(this, player);

        for (int i = 0; i < this.slots.length; i++)
        {
            this.slots[i] = new GuiSlot(i);
        }
    }

    @Override
    public void pickItem(GuiInventory inventory, ItemStack stack)
    {
        if (this.active != null)
        {
            this.active.stack = stack == null ? null : stack.copy();
            this.model.slots[this.active.slot] = this.active.stack;
            this.model.updateEntity();
            this.inventory.visible = false;
        }
    }

    @Override
    public boolean needsBackground()
    {
        return false;
    }

    @Override
    public void close()
    {
        MorphCell morph = this.morphs.morphs.getSelected();

        /* Update model's morph */
        PacketModifyModelBlock packet = new PacketModifyModelBlock(this.model.getPos(), morph == null ? null : morph.current().morph);

        packet.setBody(this.yaw.trackpad.value, this.pitch.trackpad.value, this.body.trackpad.value);
        packet.setPos(this.x.trackpad.value, this.y.trackpad.value, this.z.trackpad.value);
        packet.setRot(this.rx.trackpad.value, this.ry.trackpad.value, this.rz.trackpad.value);
        packet.setScale(this.one.button.isChecked(), this.sx.trackpad.value, this.sy.trackpad.value, this.sz.trackpad.value);
        packet.setOrder(RotationOrder.values()[this.order.button.getValue()]);
        packet.setSlots(this.model.slots);

        Dispatcher.sendToServer(packet);
    }

    public GuiModelPanel setModelBlock(TileEntityModel model)
    {
        this.model = model;
        this.temp.copyData(model);

        return this;
    }

    @Override
    public void resize(int width, int height)
    {
        if (height >= 380)
        {
            this.x.resizer().relative(this.rz.resizer).set(0, 0, 80, 20).x.set(0, Measure.PIXELS, 0);
            this.x.resizer().y.set(45, Measure.PIXELS, 0);
            this.yaw.resizer().y.set(0.5F, Measure.RELATIVE, -165);
        }
        else
        {
            this.x.resizer().parent(this.area).set(0, 20, 80, 20).x.set(1, Measure.RELATIVE, -90);
            this.x.resizer().y.set(0.5F, Measure.RELATIVE, -80);
            this.yaw.resizer().y.set(0.5F, Measure.RELATIVE, -80);
        }

        super.resize(width, height);

        this.slots[0].update(this.area.getX(0.5F) - this.area.w / 8 - 20, this.area.getY(0.5F) - 25);
        this.slots[1].update(this.area.getX(0.5F) - this.area.w / 8 - 20, this.area.getY(0.5F) + 5);

        this.slots[2].update(this.area.getX(0.5F) + this.area.w / 8, this.area.getY(0.5F) + 35);
        this.slots[3].update(this.area.getX(0.5F) + this.area.w / 8, this.area.getY(0.5F) + 5);
        this.slots[4].update(this.area.getX(0.5F) + this.area.w / 8, this.area.getY(0.5F) - 25);
        this.slots[5].update(this.area.getX(0.5F) + this.area.w / 8, this.area.getY(0.5F) - 55);
        this.inventory.update(this.area.getX(0.5F), this.area.getY(1) - 50);

        this.morphs.updateRect(this.area.x, this.area.y, this.area.w, this.area.h);
        this.morphs.setWorldAndResolution(this.mc, width, height);

        if (this.model != null)
        {
            this.morphs.morphs.setSelected(this.model.morph);

            this.yaw.trackpad.setValue(this.model.rotateYawHead);
            this.pitch.trackpad.setValue(this.model.rotatePitch);
            this.body.trackpad.setValue(this.model.rotateBody);

            this.x.trackpad.setValue(this.model.x);
            this.y.trackpad.setValue(this.model.y);
            this.z.trackpad.setValue(this.model.z);

            this.rx.trackpad.setValue(this.model.rx);
            this.ry.trackpad.setValue(this.model.ry);
            this.rz.trackpad.setValue(this.model.rz);

            this.sx.trackpad.setValue(this.model.sx);
            this.sy.trackpad.setValue(this.model.sy);
            this.sz.trackpad.setValue(this.model.sz);

            this.one.button.setIsChecked(this.model.one);
            this.order.button.setValue(this.model.order.ordinal());

            this.toggleOne();

            for (int i = 0; i < this.slots.length; i++)
            {
                this.slots[i].stack = this.model.slots[i];
            }
        }
    }

    private void toggleOne()
    {
        boolean checked = this.one.button.isChecked();

        this.model.one = checked;
        this.sy.setVisible(!checked);
        this.sz.setVisible(!checked);
    }

    @Override
    public boolean handleMouseInput(int mouseX, int mouseY) throws IOException
    {
        this.morphs.handleMouseInput();

        return !this.morphs.morphs.getHidden() && this.morphs.isInside(mouseX, mouseY);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton)
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        this.inventory.mouseClicked(mouseX, mouseY, mouseButton);
        this.active = null;

        for (GuiSlot slot : this.slots)
        {
            if (slot.area.isInside(mouseX, mouseY))
            {
                this.active = slot;
                this.inventory.visible = true;
            }
        }
    }

    @Override
    public boolean handleKeyboardInput() throws IOException
    {
        this.morphs.handleKeyboardInput();

        return !this.morphs.morphs.getHidden();
    }

    @Override
    public void draw(int mouseX, int mouseY, float partialTicks)
    {
        MorphCell cell = this.morphs.morphs.getSelected();

        if (cell != null)
        {
            int x = this.area.getX(0.5F);
            int y = this.area.getY(0.65F);

            cell.current().morph.renderOnScreen(this.mc.thePlayer, x, y, this.area.h / 4F, 1.0F);
        }

        if (this.model != null)
        {
            this.model.morph = cell == null ? null : cell.current().morph;
        }

        this.drawString(this.font, I18n.format("blockbuster.gui.model_block.entity"), this.yaw.area.x + 2, this.yaw.area.y - 12, 0xffffff);
        this.drawString(this.font, I18n.format("blockbuster.gui.model_block.translate"), this.x.area.x + 2, this.x.area.y - 12, 0xffffff);
        this.drawString(this.font, I18n.format("blockbuster.gui.model_block.rotate"), this.rx.area.x + 2, this.rx.area.y - 12, 0xffffff);
        this.drawString(this.font, I18n.format("blockbuster.gui.model_block.scale"), this.sx.area.x + 2, this.sx.area.y - 12, 0xffffff);

        super.draw(mouseX, mouseY, partialTicks);

        for (GuiSlot slot : this.slots)
        {
            slot.draw(mouseX, mouseY, partialTicks);
        }

        if (this.active != null)
        {
            Area a = this.active.area;

            Gui.drawRect(a.x, a.y, a.x + a.w, a.y + a.h, 0x880088ff);
        }

        this.inventory.draw(mouseX, mouseY, partialTicks);
        this.morphs.drawScreen(mouseX, mouseY, partialTicks);
    }
}