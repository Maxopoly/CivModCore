package vg.civcraft.mc.civmodcore.inventorygui.components;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;

import vg.civcraft.mc.civmodcore.inventorygui.IClickable;
import vg.civcraft.mc.civmodcore.inventorygui.LClickable;

public class Scrollbar extends InventoryComponent {

	private List<IClickable> unpaginatedContent;
	private int page;
	private int totalPages;
	private int backClickSlot;
	private int forwardClickSlot;
	private ContentAligner contentAligner;
	private int scrollOffset;
	
	public Scrollbar(List<IClickable> content, int staticSize) {
		this(content, staticSize, staticSize);
	}

	public Scrollbar(List<IClickable> content, int staticSize, int scrollOffset) {
		this(content, staticSize, scrollOffset, ContentAligners.getLeftAligned());
	}

	public Scrollbar(List<IClickable> content, int staticSize, int scrollOffset, ContentAligner contentAligner) {
		super(staticSize);
		this.unpaginatedContent = content;
		this.page = 0;
		this.scrollOffset = scrollOffset;
		this.backClickSlot = 0;
		this.forwardClickSlot = staticSize - 1;
		this.contentAligner = contentAligner;
		this.totalPages = calculatePageAmount();
	}

	public void setBackwardsClickSlot(int backClickSlot) {
		this.backClickSlot = backClickSlot;
	}

	public void setForwardClickSlot(int forwardClickSlot) {
		this.forwardClickSlot = forwardClickSlot;
	}

	private int calculatePageAmount() {
		int contentAmount = unpaginatedContent.size();
		int displaySize = getSize();
		if (contentAmount <= displaySize) {
			//works fine, we dont need back/forward buttons in this case
			return 1;
		}
		// first page has no back button, which messes with our modulo, so we remove the amount of items on the first page (which doesn't have a back button)
		contentAmount -= scrollOffset - 1;
		
		//modulp scroll offset - 2, because a normal page has forward and backwards buttons
		int modOffset = contentAmount % (scrollOffset - 2);
		int basicRowCalc = contentAmount / (scrollOffset - 2);
		if (modOffset == 1) {
			// there would be one leftover element in a new page, but we can just put that
			// in the previous page instead of a next button
			return basicRowCalc;
		}
		return basicRowCalc + 1;
	}

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		if (page < 0) {
			page = 0;
		}
		if (page >= totalPages) {
			page = totalPages - 1;
		}
		this.page = page;
	}

	@Override
	void rebuild() {
		int contentIndex = page * getSize();
		// subtract offset created through the next/previous button
		// note that the first page does not have a previous button
		if (page > 0) {
			contentIndex -= page;
			if (page > 1) {
				contentIndex -= page - 1;
			}
		}
		int size = getSize();
		contentAligner.reset();
		System.out.println("Size is " + size);
		for (int i = 0; i < size; i++) {
			int targetSlot = contentAligner.getNext();
			System.out.println(targetSlot);
			if (page > 0 && targetSlot == backClickSlot) {
				this.content.set(targetSlot, getBackwardClick());
			} else if (page < totalPages - 1 && targetSlot == forwardClickSlot) {
				this.content.set(targetSlot, getForwardClick());
			} else {
				if (contentIndex >= unpaginatedContent.size()) {
					this.content.set(targetSlot, null);
				}
				else {
					this.content.set(targetSlot, unpaginatedContent.get(contentIndex++));
				}
			}
		}
	}

	private IClickable getBackwardClick() {
		return new LClickable(Material.ARROW, ChatColor.GOLD + "Show previous page", p -> {
			setPage(getPage() - 1);
			update();
		});
	}

	private IClickable getForwardClick() {
		return new LClickable(Material.ARROW, ChatColor.GOLD + "Show next page", p -> {
			setPage(getPage() + 1);
			update();
		});
	}

}