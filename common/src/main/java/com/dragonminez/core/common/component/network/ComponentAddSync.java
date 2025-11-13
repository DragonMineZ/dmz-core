package com.dragonminez.core.common.component.network;

import com.dragonminez.core.common.component.model.Component;
import com.dragonminez.core.common.component.model.ComponentDescriptor;
import com.dragonminez.core.common.component.registry.ComponentRegistry;
import com.dragonminez.core.common.network.model.ISerializable;
import com.dragonminez.core.common.registry.RegistryManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;

public record ComponentAddSync(UUID holder, Component<?> component, boolean onlyPublic)
    implements ISerializable<ComponentAddSync> {

  @Override
  @SuppressWarnings({"unchecked", "rawtypes"})
  public void encode(FriendlyByteBuf buffer) {
    buffer.writeUUID(this.holder);
    buffer.writeUtf(this.component.id());
    final List<ComponentDescriptor<?, ?>> toSerialize = new ArrayList<>();
    for (ComponentDescriptor<?, ?> descriptor : this.component.descriptors()) {
      boolean shouldSerialize = !this.onlyPublic || descriptor.isPublic();
      if (shouldSerialize) {
        toSerialize.add(descriptor);
      }
    }
    buffer.writeInt(toSerialize.size());
    for (ComponentDescriptor<?, ?> descriptor : toSerialize) {
      buffer.writeUtf(descriptor.id());
      final Object value = ((ComponentDescriptor) descriptor).get(this.component);
      switch (descriptor.type()) {
        case INTEGER -> buffer.writeInt(((Number) value).intValue());
        case LONG -> buffer.writeLong(((Number) value).longValue());
        case FLOAT -> buffer.writeFloat(((Number) value).floatValue());
        case DOUBLE -> buffer.writeDouble(((Number) value).doubleValue());
        case BOOLEAN -> buffer.writeBoolean((Boolean) value);
        case STRING -> buffer.writeUtf((String) value);
        default ->
            throw new IllegalStateException("Unsupported descriptor type: " + descriptor.type());
      }
    }
  }

  @Override
  @SuppressWarnings({"unchecked", "rawtypes"})
  public ComponentAddSync decode(FriendlyByteBuf buffer) {
    final UUID holder = buffer.readUUID();
    final String componentId = buffer.readUtf();
    final int count = buffer.readInt();

    final Optional<Component<?>> optTemplate = RegistryManager.getValue(ComponentRegistry.class,
        componentId);
    if (optTemplate.isEmpty()) {
      throw new IllegalStateException("Failed to decode component. "
          + "Unknown component id " + componentId);
    }

    final Component<?> template = optTemplate.get();
    final Component<?> instance = template.create();
    for (int i = 0; i < count; i++) {
      final String descriptorId = buffer.readUtf();
      final Optional<? extends ComponentDescriptor<?, ?>> opt = template.descriptors().stream()
          .filter(d -> d.id().equals(descriptorId))
          .findFirst();
      if (opt.isEmpty()) {
        throw new IllegalStateException(
            "Unknown descriptor id " + descriptorId + " for component " + componentId);
      }
      final ComponentDescriptor descriptor = opt.get();
      final Object value;
      switch (descriptor.type()) {
        case INTEGER -> value = buffer.readInt();
        case LONG -> value = buffer.readLong();
        case FLOAT -> value = buffer.readFloat();
        case DOUBLE -> value = buffer.readDouble();
        case BOOLEAN -> value = buffer.readBoolean();
        case STRING -> value = buffer.readUtf();
        default ->
            throw new IllegalStateException("Unsupported descriptor type: " + descriptor.type());
      }
      descriptor.set(instance, value);
    }
    return new ComponentAddSync(holder, instance, onlyPublic);
  }
}
