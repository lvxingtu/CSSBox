package org.fit.cssbox.layout;

import cz.vutbr.web.css.CSSProperty;

import java.util.ArrayList;

/**
 * Flex line for "storing" and aligning flex items, which are in the same line in vertical (column) container.
 */
public class FlexLineColumn extends FlexLine{

    /** Saved start remaining width used for align-content: stretch */
    protected int savedWidth;

    /** X coordinate of left side of flex line */
    protected int x;

    /** Creates new instance of flex line for vetical container
     * @param owner container
     */
    public FlexLineColumn(FlexContainerBlockBox owner) {
        this.owner = owner;
        x = 0;
        height = owner.getContentHeight();
        itemsInLine = new ArrayList<>();
        isFirstItem = true;
        width = 0;
        remainingMainSpace = height;
        savedWidth = -1;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    @Override
    protected void applyAlignContent(ArrayList<FlexLine> lines, int countOfPreviousLines) {
        if(owner.crossSize == 0 || owner.flexWrap == FlexContainerBlockBox.FLEX_WRAP_NOWRAP) //is container width set? is container nowrap?
            return;

        int remainingWidthSpace = owner.crossSize;
        for (int iline = 0; iline < lines.size(); iline++) {
            FlexLineColumn line = (FlexLineColumn) lines.get(iline);
            if(line.savedWidth == -1)
                remainingWidthSpace -= line.getWidth();
            else
                remainingWidthSpace -= line.savedWidth;
        }

        if(owner.alignContent == FlexContainerBlockBox.ALIGN_CONTENT_FLEX_START){
            if(owner.flexWrap == FlexContainerBlockBox.FLEX_WRAP_WRAP_REVERSE)
                setX(getX()+remainingWidthSpace);
            else
                return; //was already set

        } else if(owner.alignContent == FlexContainerBlockBox.ALIGN_CONTENT_FLEX_END){
            if(owner.flexWrap == FlexContainerBlockBox.FLEX_WRAP_WRAP_REVERSE)
                return; //was already set
            else
                setX(getX()+remainingWidthSpace);

        } else if(owner.alignContent == FlexContainerBlockBox.ALIGN_CONTENT_CENTER){
            setX(getX()+remainingWidthSpace/2); //wrap reverse is same (just centerize)

        } else if(owner.alignContent == FlexContainerBlockBox.ALIGN_CONTENT_STRETCH){
            if (remainingWidthSpace < 0) {
                //no space - it is like flex-start
                if (owner.flexWrap == FlexContainerBlockBox.FLEX_WRAP_WRAP_REVERSE)
                    setX(getX() + remainingWidthSpace);
                //else do nothing
                    return;
            }

            savedWidth = getWidth();
            setWidth(getWidth() + remainingWidthSpace/lines.size());

        } else if(owner.alignContent == FlexContainerBlockBox.ALIGN_CONTENT_SPACE_BETWEEN) {
            if (remainingWidthSpace < 0) {

                //no space - it is like flex-start
                if (owner.flexWrap == FlexContainerBlockBox.FLEX_WRAP_WRAP_REVERSE)
                    setX(getX() + remainingWidthSpace);
                //else do nothing

                    return;
            }

            if(lines.size() != 1)
                setX(getX()+(countOfPreviousLines)*remainingWidthSpace/(lines.size()-1)); //wrap reverse is same
            else
            if(owner.flexWrap == FlexContainerBlockBox.FLEX_WRAP_WRAP_REVERSE) //wrap reverse is same as flex-end
                setX(getX()+remainingWidthSpace);
            else
                return; //was already set as flex-start
        } else if(owner.alignContent == FlexContainerBlockBox.ALIGN_CONTENT_SPACE_AROUND){
            if (remainingWidthSpace < 0) {
                setX(getX()+remainingWidthSpace/2); //wrap reverse is same (just centerize)
                return;
            }

            if(countOfPreviousLines == 0)
                setX(getX()+ remainingWidthSpace/ 2 / lines.size());
            else
                setX(getX() + remainingWidthSpace/ 2 / lines.size() +(countOfPreviousLines)*remainingWidthSpace/ lines.size()+1);

        }
    }

    @Override
    protected void setCrossCoordToItem(FlexItemBlockBox item) {

        if(applyAlignSelf(item))
            return;

        if (owner.alignItems == FlexContainerBlockBox.ALIGN_ITEMS_FLEX_START || owner.alignItems == FlexContainerBlockBox.ALIGN_ITEMS_BASELINE)
            if(owner.flexWrap == FlexContainerBlockBox.FLEX_WRAP_WRAP_REVERSE)
                item.setPosition(x + getWidth() - item.getWidth(), 0);
            else
                item.setPosition(x, 0);

        else if (owner.alignItems == FlexContainerBlockBox.ALIGN_ITEMS_FLEX_END)
            if(owner.flexWrap == FlexContainerBlockBox.FLEX_WRAP_WRAP_REVERSE)
                item.setPosition(x, 0);
            else
                item.setPosition(x + getWidth() - item.getWidth(), 0);

        else if (owner.alignItems == FlexContainerBlockBox.ALIGN_ITEMS_CENTER)
            item.setPosition(x + (getWidth() - item.getWidth()) / 2, 0);

        else
            alignStretch(item);
    }

    @Override
    protected boolean applyAlignSelf(FlexItemBlockBox item){
        if(!item.isNotAlignSelfAuto())
            return false;


        if (getWidth() < item.getWidth())
            if(!(owner.flexWrap == FlexContainerBlockBox.FLEX_WRAP_NOWRAP && owner.hasFixedWidth()))
                setWidth(item.getWidth());

        if(item.alignSelf == CSSProperty.AlignSelf.INHERIT) {
            CSSProperty.AlignSelf parentAlignSelf =  owner.style.getProperty("align-self");
            if(parentAlignSelf != null && parentAlignSelf != CSSProperty.AlignSelf.AUTO && parentAlignSelf != CSSProperty.AlignSelf.INHERIT && parentAlignSelf != CSSProperty.AlignSelf.INITIAL){
                item.alignSelf = parentAlignSelf;
            }
        } else if(item.alignSelf == FlexItemBlockBox.ALIGN_SELF_FLEX_START || item.alignSelf == FlexItemBlockBox.ALIGN_SELF_BASELINE)
            if(owner.flexWrap == FlexContainerBlockBox.FLEX_WRAP_WRAP_REVERSE)
                item.setPosition(x + getWidth() - item.getWidth(), 0);
            else
                item.setPosition(x, 0);
        else if(item.alignSelf == FlexItemBlockBox.ALIGN_SELF_FLEX_END)
            if(owner.flexWrap == FlexContainerBlockBox.FLEX_WRAP_WRAP_REVERSE)
                item.setPosition(x, 0);
            else
                item.setPosition(x + getWidth() - item.getWidth(), 0);
        else if(item.alignSelf == FlexItemBlockBox.ALIGN_SELF_CENTER)
            item.setPosition(x + (getWidth() - item.getWidth()) / 2, 0);
        else //STRETCH
            alignStretch(item);

        return true;
    }

    /**
     * Aligns item according to align-items (or align-self) value stretch.
     * @param item item to align
     */
    private void alignStretch(FlexItemBlockBox item){
        if(item.crossSizeSetByCont) {
            item.bounds.width = getWidth();
            if (item.bounds.width > item.max_size.width && item.max_size.width != -1) {
                item.bounds.width = item.max_size.width + item.padding.left + item.padding.right + item.border.left + item.border.right + item.margin.left + item.margin.right;
            }
        }

        if(owner.flexWrap == CSSProperty.FlexWrap.NOWRAP){
            if(owner.hasFixedWidth() && getWidth() > owner.getContentWidth()) {
                if(!item.hasFixedWidth()) {
                    if(owner.style.getProperty("width") != CSSProperty.Width.length)
                        owner.setContentWidth(getWidth());

                    item.bounds.width = owner.getContentWidth();
                    if (item.bounds.width > item.max_size.width && item.max_size.width != -1) {
                        item.bounds.width = item.max_size.width + item.padding.left + item.padding.right + item.border.left + item.border.right + item.margin.left + item.margin.right;
                    }
                }
            }

        }
        item.content.width = item.bounds.width - item.padding.left - item.padding.right - item.border.left - item.border.right - item.margin.left - item.margin.right;
        if(owner.flexWrap == FlexContainerBlockBox.FLEX_WRAP_WRAP_REVERSE && owner.alignContent == FlexContainerBlockBox.ALIGN_CONTENT_STRETCH)
            item.setPosition(x +getWidth() - item.getWidth(), 0);
        else
            item.setPosition(x, 0);

    }



    @Override
    protected void setPositionAndAdjustCrossSize(FlexItemBlockBox item) {
        if (getWidth() < item.getWidth()) {
            if(!(owner.flexWrap == FlexContainerBlockBox.FLEX_WRAP_NOWRAP && owner.hasFixedWidth()))
                setWidth(item.getWidth());

            //width of line has changed (for items already in itemsInLine)
            if (owner.alignItems == FlexContainerBlockBox.ALIGN_ITEMS_STRETCH) {
                for (int y = 0; y < itemsInLine.size(); y++) {
                    FlexItemBlockBox itemInLine = itemsInLine.get(y);
                    if((itemInLine.alignSelf == FlexItemBlockBox.ALIGN_SELF_AUTO && owner.alignItems == FlexContainerBlockBox.ALIGN_ITEMS_STRETCH)
                            || itemInLine.alignSelf == FlexItemBlockBox.ALIGN_SELF_STRETCH) {

                        //if item is set by width value, it does not stretch
                        if (!itemInLine.hasFixedWidth() && !itemInLine.isNotAlignSelfAuto() && itemInLine.alignSelf != FlexItemBlockBox.ALIGN_SELF_STRETCH) {

                            itemInLine.bounds.width = getWidth();
                            if (itemInLine.bounds.width >itemInLine.max_size.width && itemInLine.max_size.width != -1) {
                                itemInLine.bounds.width = itemInLine.max_size.width + itemInLine.padding.left + itemInLine.padding.right +
                                        itemInLine.border.left + itemInLine.border.right+
                                        itemInLine.margin.left + itemInLine.margin.right;
                            }
                            itemInLine.content.width = itemInLine.bounds.width - itemInLine.padding.left - itemInLine.padding.right -
                                    itemInLine.border.left - itemInLine.border.right - itemInLine.margin.left - itemInLine.margin.right;
                        }
                    }

                }
            } else if (owner.alignItems == FlexContainerBlockBox.ALIGN_ITEMS_FLEX_END) {
                for (int c = 0; c < itemsInLine.size(); c++) {
                    itemsInLine.get(c).setPosition(x + getWidth() - itemsInLine.get(c).getWidth()  , itemsInLine.get(c).bounds.y);
                }
            } else if (owner.alignItems == FlexContainerBlockBox.ALIGN_ITEMS_CENTER) {
                for (int c = 0; c < itemsInLine.size(); c++) {
                    itemsInLine.get(c).setPosition( x + (getWidth() - itemsInLine.get(c).getWidth()) / 2, itemsInLine.get(c).bounds.y);
                }
            }
        }

        setCrossCoordToItem(item);

        for (int c = 0; c < itemsInLine.size(); c++) {
            if(itemsInLine.get(c).alignSelf != CSSProperty.AlignSelf.AUTO){
                applyAlignSelf(itemsInLine.get(c));
            }
        }
    }

    @Override
    protected void setLineCrossSize() {

        if (owner.flexWrap == FlexContainerBlockBox.FLEX_WRAP_NOWRAP) {
            setWidth(owner.crossSize);
            return;
        }

            int width = 0;
            for (FlexItemBlockBox item : itemsInLine) {
                if (width< item.bounds.width)
                    width = item.bounds.width;
            }
            setWidth(width);
    }

    @Override
    protected void applyAlignItemsAndSelf() {
        for (int j = 0; j < itemsInLine.size(); j++) {
            if (j == 0) {
                //is first item, so it is easier to handle (it does not need to fix previous items)
                setCrossCoordToItem(itemsInLine.get(j));
                continue;
            }
            setPositionAndAdjustCrossSize(itemsInLine.get(j));
        }
    }

    @Override
    protected void alignByJustifyContent(){
        int heightOfPreviousItems = 0;
        int totalHeightOfItems = 0;
        for(int j = 0; j < itemsInLine.size(); j++)
            totalHeightOfItems += itemsInLine.get(j).getHeight();

        if((owner.isDirectionReversed() || owner.justifyContent == FlexContainerBlockBox.JUSTIFY_CONTENT_FLEX_END) &&
                owner.justifyContent != FlexContainerBlockBox.JUSTIFY_CONTENT_SPACE_BETWEEN &&
                owner.justifyContent != FlexContainerBlockBox.JUSTIFY_CONTENT_FLEX_START) {
            for (int j = itemsInLine.size() - 1; j >= 0; j--) {
                FlexItemBlockBox item = itemsInLine.get(j);
                applyJustifyContent(item, heightOfPreviousItems, j, totalHeightOfItems);
                heightOfPreviousItems += item.getHeight();

            }
        } else {
            for (int j = 0; j < itemsInLine.size(); j++) {
                FlexItemBlockBox item = itemsInLine.get(j);
                applyJustifyContent(item, heightOfPreviousItems, j, totalHeightOfItems);
                heightOfPreviousItems += item.getHeight();
            }
        }
    }

    @Override
    protected void applyJustifyContent(FlexItemBlockBox item, int heightOfPreviousItems, int j, int totalHeightOfItems){

        if  (owner.justifyContent == FlexContainerBlockBox.JUSTIFY_CONTENT_FLEX_END) {
            if (owner.isDirectionReversed())
                item.setPosition(item.bounds.x, heightOfPreviousItems);
            else
                item.setPosition(item.bounds.x, owner.mainSize - heightOfPreviousItems - item.bounds.height);

        } else if (owner.justifyContent == FlexContainerBlockBox.JUSTIFY_CONTENT_CENTER) {
            int halfOfRemainSpace = (owner.mainSize) / 2;
            item.setPosition(item.bounds.x, halfOfRemainSpace - totalHeightOfItems/2 + heightOfPreviousItems );

        } else if (owner.justifyContent == FlexContainerBlockBox.JUSTIFY_CONTENT_SPACE_BETWEEN) {
            if(itemsInLine.size()-1 == 0 || totalHeightOfItems > owner.mainSize){
                //only one item or items are outside of container - it works like start
                if (owner.isDirectionReversed())
                    item.setPosition(item.bounds.x, owner.mainSize - heightOfPreviousItems - item.bounds.height );
                else
                    item.setPosition(item.bounds.x, heightOfPreviousItems);

            } else {
                int spaceBetween = 0;

                if(owner.isDirectionReversed()) {
                    if(j == 0) {
                        item.setPosition(item.bounds.x, owner.mainSize -item.bounds.height);
                    } else if (j == itemsInLine.size()-1) {
                        item.setPosition(item.bounds.x, 0);
                    } else {//it is not first item in row
                        spaceBetween = getRemainingMainSpace() / (itemsInLine.size() - 1);
                        item.setPosition(item.bounds.x,spaceBetween * (itemsInLine.size()-1 -j) + totalHeightOfItems - heightOfPreviousItems - item.bounds.height);
                    }
                } else {
                    if (j == 0) {
                        item.setPosition(item.bounds.x, 0);
                    } else {//it is not first item in row
                        spaceBetween = getRemainingMainSpace() / (itemsInLine.size() - 1);
                        item.setPosition(item.bounds.x, spaceBetween * j + heightOfPreviousItems);
                    }
                }
            }

        } else if (owner.justifyContent == FlexContainerBlockBox.JUSTIFY_CONTENT_SPACE_AROUND) {
            if(itemsInLine.size()-1 == 0 || totalHeightOfItems > owner.mainSize){
                //only one item - it works like center
                int halfOfRemainSpace = (owner.bounds.y + owner.mainSize) / 2;
                item.setPosition(item.bounds.x, halfOfRemainSpace - totalHeightOfItems/2 + heightOfPreviousItems);
            } else {
                //more than one item
                int spaceAround = getRemainingMainSpace() / (itemsInLine.size());
                spaceAround /= 2;
                if(owner.isDirectionReversed()){
                    item.setPosition(item.bounds.x, spaceAround + (itemsInLine.size()-1 -j) * 2 * spaceAround + heightOfPreviousItems);
                } else {
                    item.setPosition( item.bounds.x, j * 2 * spaceAround + heightOfPreviousItems + spaceAround);
                }
            }

        } else {
            //flex start
            if (owner.isDirectionReversed())
                item.setPosition(item.bounds.x, owner.mainSize - heightOfPreviousItems - item.bounds.height );
            else
                item.setPosition(item.bounds.x, heightOfPreviousItems);
        }
    }
}