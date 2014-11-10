package mezz.jei.gui;

import mezz.jei.util.StackUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GuiItemStack {

	public static final int baseWidth = 16;
	public static final int baseHeight = 16;
	private static final RenderItem itemRender = new RenderItem();

	public final int width;
	public final int height;
	public final int padding;
	/* the amount of time in ms to display one itemStack before cycling to the next one */
	protected final int cycleTime = 1000;

	public int xPosition;
	public int yPosition;
	public boolean enabled;
	public boolean visible;

	protected List<ItemStack> itemStacks = new ArrayList<ItemStack>();
	protected long drawTime = 0;

	public GuiItemStack(int xPosition, int yPosition, int padding) {
		this.xPosition = xPosition;
		this.yPosition = yPosition;
		this.padding = padding;
		this.width = getWidth(padding);
		this.height = getHeight(padding);
	}

	public static int getWidth(int padding) {
		return baseWidth + (2 * padding);
	}

	public static int getHeight(int padding) {
		return baseHeight + (2 * padding);
	}

	public void setItemStacks(Object obj, ItemStack focusStack) {
		if (obj == null) {
			clearItemStacks();
		}
		else if (obj instanceof ItemStack) {
			setItemStack((ItemStack) obj);
		}
		else if (obj instanceof List) {
			List list = (List)obj;
			List<ItemStack> itemStacks = StackUtil.getItemStacksRecursive(list);
			if (StackUtil.containsStack(itemStacks, focusStack))
				setItemStack(focusStack);
			else
				setItemStacks(itemStacks);
		}
		else {
			throw new IllegalArgumentException("Tried to set something other than an ItemStack or list of ItemStacks: " + obj);
		}
	}

	public void setItemStacks(List<ItemStack> itemStacks) {
		this.itemStacks = StackUtil.getItemStacksRecursive(itemStacks);
		visible = enabled = !itemStacks.isEmpty();
	}

	public void clearItemStacks() {
		itemStacks = new ArrayList<ItemStack>();
		visible = enabled = false;
	}

	public ItemStack getItemStack() {
		if (itemStacks.isEmpty())
			return null;

		int stackIndex = (int)((drawTime / cycleTime) % itemStacks.size());
		return itemStacks.get(stackIndex);
	}

	public void setItemStack(ItemStack itemStacks) {
		setItemStacks(Arrays.asList(itemStacks));
	}

	public boolean isMouseOver(int mouseX, int mouseY) {
		return enabled && visible && (mouseX >= xPosition) && (mouseY >= yPosition) && (mouseX < xPosition + width) && (mouseY < yPosition + height);
	}

	public void draw(Minecraft minecraft) {
		draw(minecraft, true);
	}

	public void drawHovered(Minecraft minecraft, int mouseX, int mouseY) {
		draw(minecraft, false);
		minecraft.currentScreen.renderToolTip(getItemStack(), mouseX, mouseY);
		RenderHelper.disableStandardItemLighting();
	}

	public void draw(Minecraft minecraft, boolean cycleIcons) {
		if (!visible)
			return;

		if (cycleIcons)
			drawTime = System.currentTimeMillis();

		ItemStack itemStack = getItemStack();
		FontRenderer font = itemStack.getItem().getFontRenderer(itemStack);
		if (font == null)
			font = minecraft.fontRenderer;

		GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
		{
			RenderHelper.enableGUIStandardItemLighting();
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glEnable(GL12.GL_RESCALE_NORMAL);
			GL11.glEnable(GL11.GL_COLOR_MATERIAL);
			GL11.glEnable(GL11.GL_LIGHTING);
			itemRender.zLevel = 100.0F;

			itemRender.renderItemAndEffectIntoGUI(font, minecraft.getTextureManager(), itemStack, xPosition + padding, yPosition + padding);
			itemRender.renderItemOverlayIntoGUI(font, minecraft.getTextureManager(), itemStack, xPosition + padding, yPosition + padding);

			itemRender.zLevel = 0.0F;
		}
		GL11.glPopAttrib();
	}
}
