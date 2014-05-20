/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package goods;

import com.google.common.base.Preconditions;

import java.util.LinkedList;
import java.util.Queue;

/**
 * This inventory section is a simple queue
 * Created by carrknight on 5/12/14.
 */
public class InventorySectionStorage implements InventorySection
{

    private final Queue<Good> delegate;

    private final GoodType type;

    public InventorySectionStorage(GoodType type) {
        Preconditions.checkArgument(type.isDifferentiated(), "storage is pointless for undifferentiated goods");
        this.delegate = new LinkedList<>();
        this.type = type;
    }


    @Override
    public void store(Good good) {
        delegate.add(good);

    }

    @Override
    public void remove(Good good) {
        delegate.remove(good);
    }

    @Override
    public int removeAll() {
        int delegateSize = size();
        delegate.clear();
        return delegateSize;
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public GoodType getGoodType() {
        return type;
    }

    @Override
    public boolean containSpecificGood(Good g) {
        return delegate.contains(g);
    }

    @Override
    public Good peek() {
        return delegate.peek();
    }
}
