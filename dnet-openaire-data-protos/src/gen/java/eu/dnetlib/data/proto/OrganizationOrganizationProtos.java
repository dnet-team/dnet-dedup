// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: Organization_Organization.proto

package eu.dnetlib.data.proto;

public final class OrganizationOrganizationProtos {
  private OrganizationOrganizationProtos() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
  }
  public interface OrganizationOrganizationOrBuilder
      extends com.google.protobuf.MessageOrBuilder {
    
    // optional .eu.dnetlib.data.proto.Dedup dedup = 1;
    boolean hasDedup();
    eu.dnetlib.data.proto.DedupProtos.Dedup getDedup();
    eu.dnetlib.data.proto.DedupProtos.DedupOrBuilder getDedupOrBuilder();
    
    // optional .eu.dnetlib.data.proto.DedupSimilarity dedupSimilarity = 2;
    boolean hasDedupSimilarity();
    eu.dnetlib.data.proto.DedupSimilarityProtos.DedupSimilarity getDedupSimilarity();
    eu.dnetlib.data.proto.DedupSimilarityProtos.DedupSimilarityOrBuilder getDedupSimilarityOrBuilder();
  }
  public static final class OrganizationOrganization extends
      com.google.protobuf.GeneratedMessage
      implements OrganizationOrganizationOrBuilder {
    // Use OrganizationOrganization.newBuilder() to construct.
    private OrganizationOrganization(Builder builder) {
      super(builder);
    }
    private OrganizationOrganization(boolean noInit) {}
    
    private static final OrganizationOrganization defaultInstance;
    public static OrganizationOrganization getDefaultInstance() {
      return defaultInstance;
    }
    
    public OrganizationOrganization getDefaultInstanceForType() {
      return defaultInstance;
    }
    
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return eu.dnetlib.data.proto.OrganizationOrganizationProtos.internal_static_eu_dnetlib_data_proto_OrganizationOrganization_descriptor;
    }
    
    protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return eu.dnetlib.data.proto.OrganizationOrganizationProtos.internal_static_eu_dnetlib_data_proto_OrganizationOrganization_fieldAccessorTable;
    }
    
    private int bitField0_;
    // optional .eu.dnetlib.data.proto.Dedup dedup = 1;
    public static final int DEDUP_FIELD_NUMBER = 1;
    private eu.dnetlib.data.proto.DedupProtos.Dedup dedup_;
    public boolean hasDedup() {
      return ((bitField0_ & 0x00000001) == 0x00000001);
    }
    public eu.dnetlib.data.proto.DedupProtos.Dedup getDedup() {
      return dedup_;
    }
    public eu.dnetlib.data.proto.DedupProtos.DedupOrBuilder getDedupOrBuilder() {
      return dedup_;
    }
    
    // optional .eu.dnetlib.data.proto.DedupSimilarity dedupSimilarity = 2;
    public static final int DEDUPSIMILARITY_FIELD_NUMBER = 2;
    private eu.dnetlib.data.proto.DedupSimilarityProtos.DedupSimilarity dedupSimilarity_;
    public boolean hasDedupSimilarity() {
      return ((bitField0_ & 0x00000002) == 0x00000002);
    }
    public eu.dnetlib.data.proto.DedupSimilarityProtos.DedupSimilarity getDedupSimilarity() {
      return dedupSimilarity_;
    }
    public eu.dnetlib.data.proto.DedupSimilarityProtos.DedupSimilarityOrBuilder getDedupSimilarityOrBuilder() {
      return dedupSimilarity_;
    }
    
    private void initFields() {
      dedup_ = eu.dnetlib.data.proto.DedupProtos.Dedup.getDefaultInstance();
      dedupSimilarity_ = eu.dnetlib.data.proto.DedupSimilarityProtos.DedupSimilarity.getDefaultInstance();
    }
    private byte memoizedIsInitialized = -1;
    public final boolean isInitialized() {
      byte isInitialized = memoizedIsInitialized;
      if (isInitialized != -1) return isInitialized == 1;
      
      if (hasDedup()) {
        if (!getDedup().isInitialized()) {
          memoizedIsInitialized = 0;
          return false;
        }
      }
      if (hasDedupSimilarity()) {
        if (!getDedupSimilarity().isInitialized()) {
          memoizedIsInitialized = 0;
          return false;
        }
      }
      memoizedIsInitialized = 1;
      return true;
    }
    
    public void writeTo(com.google.protobuf.CodedOutputStream output)
                        throws java.io.IOException {
      getSerializedSize();
      if (((bitField0_ & 0x00000001) == 0x00000001)) {
        output.writeMessage(1, dedup_);
      }
      if (((bitField0_ & 0x00000002) == 0x00000002)) {
        output.writeMessage(2, dedupSimilarity_);
      }
      getUnknownFields().writeTo(output);
    }
    
    private int memoizedSerializedSize = -1;
    public int getSerializedSize() {
      int size = memoizedSerializedSize;
      if (size != -1) return size;
    
      size = 0;
      if (((bitField0_ & 0x00000001) == 0x00000001)) {
        size += com.google.protobuf.CodedOutputStream
          .computeMessageSize(1, dedup_);
      }
      if (((bitField0_ & 0x00000002) == 0x00000002)) {
        size += com.google.protobuf.CodedOutputStream
          .computeMessageSize(2, dedupSimilarity_);
      }
      size += getUnknownFields().getSerializedSize();
      memoizedSerializedSize = size;
      return size;
    }
    
    private static final long serialVersionUID = 0L;
    @java.lang.Override
    protected java.lang.Object writeReplace()
        throws java.io.ObjectStreamException {
      return super.writeReplace();
    }
    
    public static eu.dnetlib.data.proto.OrganizationOrganizationProtos.OrganizationOrganization parseFrom(
        com.google.protobuf.ByteString data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return newBuilder().mergeFrom(data).buildParsed();
    }
    public static eu.dnetlib.data.proto.OrganizationOrganizationProtos.OrganizationOrganization parseFrom(
        com.google.protobuf.ByteString data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return newBuilder().mergeFrom(data, extensionRegistry)
               .buildParsed();
    }
    public static eu.dnetlib.data.proto.OrganizationOrganizationProtos.OrganizationOrganization parseFrom(byte[] data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return newBuilder().mergeFrom(data).buildParsed();
    }
    public static eu.dnetlib.data.proto.OrganizationOrganizationProtos.OrganizationOrganization parseFrom(
        byte[] data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return newBuilder().mergeFrom(data, extensionRegistry)
               .buildParsed();
    }
    public static eu.dnetlib.data.proto.OrganizationOrganizationProtos.OrganizationOrganization parseFrom(java.io.InputStream input)
        throws java.io.IOException {
      return newBuilder().mergeFrom(input).buildParsed();
    }
    public static eu.dnetlib.data.proto.OrganizationOrganizationProtos.OrganizationOrganization parseFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return newBuilder().mergeFrom(input, extensionRegistry)
               .buildParsed();
    }
    public static eu.dnetlib.data.proto.OrganizationOrganizationProtos.OrganizationOrganization parseDelimitedFrom(java.io.InputStream input)
        throws java.io.IOException {
      Builder builder = newBuilder();
      if (builder.mergeDelimitedFrom(input)) {
        return builder.buildParsed();
      } else {
        return null;
      }
    }
    public static eu.dnetlib.data.proto.OrganizationOrganizationProtos.OrganizationOrganization parseDelimitedFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      Builder builder = newBuilder();
      if (builder.mergeDelimitedFrom(input, extensionRegistry)) {
        return builder.buildParsed();
      } else {
        return null;
      }
    }
    public static eu.dnetlib.data.proto.OrganizationOrganizationProtos.OrganizationOrganization parseFrom(
        com.google.protobuf.CodedInputStream input)
        throws java.io.IOException {
      return newBuilder().mergeFrom(input).buildParsed();
    }
    public static eu.dnetlib.data.proto.OrganizationOrganizationProtos.OrganizationOrganization parseFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return newBuilder().mergeFrom(input, extensionRegistry)
               .buildParsed();
    }
    
    public static Builder newBuilder() { return Builder.create(); }
    public Builder newBuilderForType() { return newBuilder(); }
    public static Builder newBuilder(eu.dnetlib.data.proto.OrganizationOrganizationProtos.OrganizationOrganization prototype) {
      return newBuilder().mergeFrom(prototype);
    }
    public Builder toBuilder() { return newBuilder(this); }
    
    @java.lang.Override
    protected Builder newBuilderForType(
        com.google.protobuf.GeneratedMessage.BuilderParent parent) {
      Builder builder = new Builder(parent);
      return builder;
    }
    public static final class Builder extends
        com.google.protobuf.GeneratedMessage.Builder<Builder>
       implements eu.dnetlib.data.proto.OrganizationOrganizationProtos.OrganizationOrganizationOrBuilder {
      public static final com.google.protobuf.Descriptors.Descriptor
          getDescriptor() {
        return eu.dnetlib.data.proto.OrganizationOrganizationProtos.internal_static_eu_dnetlib_data_proto_OrganizationOrganization_descriptor;
      }
      
      protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
          internalGetFieldAccessorTable() {
        return eu.dnetlib.data.proto.OrganizationOrganizationProtos.internal_static_eu_dnetlib_data_proto_OrganizationOrganization_fieldAccessorTable;
      }
      
      // Construct using eu.dnetlib.data.proto.OrganizationOrganizationProtos.OrganizationOrganization.newBuilder()
      private Builder() {
        maybeForceBuilderInitialization();
      }
      
      private Builder(BuilderParent parent) {
        super(parent);
        maybeForceBuilderInitialization();
      }
      private void maybeForceBuilderInitialization() {
        if (com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders) {
          getDedupFieldBuilder();
          getDedupSimilarityFieldBuilder();
        }
      }
      private static Builder create() {
        return new Builder();
      }
      
      public Builder clear() {
        super.clear();
        if (dedupBuilder_ == null) {
          dedup_ = eu.dnetlib.data.proto.DedupProtos.Dedup.getDefaultInstance();
        } else {
          dedupBuilder_.clear();
        }
        bitField0_ = (bitField0_ & ~0x00000001);
        if (dedupSimilarityBuilder_ == null) {
          dedupSimilarity_ = eu.dnetlib.data.proto.DedupSimilarityProtos.DedupSimilarity.getDefaultInstance();
        } else {
          dedupSimilarityBuilder_.clear();
        }
        bitField0_ = (bitField0_ & ~0x00000002);
        return this;
      }
      
      public Builder clone() {
        return create().mergeFrom(buildPartial());
      }
      
      public com.google.protobuf.Descriptors.Descriptor
          getDescriptorForType() {
        return eu.dnetlib.data.proto.OrganizationOrganizationProtos.OrganizationOrganization.getDescriptor();
      }
      
      public eu.dnetlib.data.proto.OrganizationOrganizationProtos.OrganizationOrganization getDefaultInstanceForType() {
        return eu.dnetlib.data.proto.OrganizationOrganizationProtos.OrganizationOrganization.getDefaultInstance();
      }
      
      public eu.dnetlib.data.proto.OrganizationOrganizationProtos.OrganizationOrganization build() {
        eu.dnetlib.data.proto.OrganizationOrganizationProtos.OrganizationOrganization result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(result);
        }
        return result;
      }
      
      private eu.dnetlib.data.proto.OrganizationOrganizationProtos.OrganizationOrganization buildParsed()
          throws com.google.protobuf.InvalidProtocolBufferException {
        eu.dnetlib.data.proto.OrganizationOrganizationProtos.OrganizationOrganization result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(
            result).asInvalidProtocolBufferException();
        }
        return result;
      }
      
      public eu.dnetlib.data.proto.OrganizationOrganizationProtos.OrganizationOrganization buildPartial() {
        eu.dnetlib.data.proto.OrganizationOrganizationProtos.OrganizationOrganization result = new eu.dnetlib.data.proto.OrganizationOrganizationProtos.OrganizationOrganization(this);
        int from_bitField0_ = bitField0_;
        int to_bitField0_ = 0;
        if (((from_bitField0_ & 0x00000001) == 0x00000001)) {
          to_bitField0_ |= 0x00000001;
        }
        if (dedupBuilder_ == null) {
          result.dedup_ = dedup_;
        } else {
          result.dedup_ = dedupBuilder_.build();
        }
        if (((from_bitField0_ & 0x00000002) == 0x00000002)) {
          to_bitField0_ |= 0x00000002;
        }
        if (dedupSimilarityBuilder_ == null) {
          result.dedupSimilarity_ = dedupSimilarity_;
        } else {
          result.dedupSimilarity_ = dedupSimilarityBuilder_.build();
        }
        result.bitField0_ = to_bitField0_;
        onBuilt();
        return result;
      }
      
      public Builder mergeFrom(com.google.protobuf.Message other) {
        if (other instanceof eu.dnetlib.data.proto.OrganizationOrganizationProtos.OrganizationOrganization) {
          return mergeFrom((eu.dnetlib.data.proto.OrganizationOrganizationProtos.OrganizationOrganization)other);
        } else {
          super.mergeFrom(other);
          return this;
        }
      }
      
      public Builder mergeFrom(eu.dnetlib.data.proto.OrganizationOrganizationProtos.OrganizationOrganization other) {
        if (other == eu.dnetlib.data.proto.OrganizationOrganizationProtos.OrganizationOrganization.getDefaultInstance()) return this;
        if (other.hasDedup()) {
          mergeDedup(other.getDedup());
        }
        if (other.hasDedupSimilarity()) {
          mergeDedupSimilarity(other.getDedupSimilarity());
        }
        this.mergeUnknownFields(other.getUnknownFields());
        return this;
      }
      
      public final boolean isInitialized() {
        if (hasDedup()) {
          if (!getDedup().isInitialized()) {
            
            return false;
          }
        }
        if (hasDedupSimilarity()) {
          if (!getDedupSimilarity().isInitialized()) {
            
            return false;
          }
        }
        return true;
      }
      
      public Builder mergeFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
        com.google.protobuf.UnknownFieldSet.Builder unknownFields =
          com.google.protobuf.UnknownFieldSet.newBuilder(
            this.getUnknownFields());
        while (true) {
          int tag = input.readTag();
          switch (tag) {
            case 0:
              this.setUnknownFields(unknownFields.build());
              onChanged();
              return this;
            default: {
              if (!parseUnknownField(input, unknownFields,
                                     extensionRegistry, tag)) {
                this.setUnknownFields(unknownFields.build());
                onChanged();
                return this;
              }
              break;
            }
            case 10: {
              eu.dnetlib.data.proto.DedupProtos.Dedup.Builder subBuilder = eu.dnetlib.data.proto.DedupProtos.Dedup.newBuilder();
              if (hasDedup()) {
                subBuilder.mergeFrom(getDedup());
              }
              input.readMessage(subBuilder, extensionRegistry);
              setDedup(subBuilder.buildPartial());
              break;
            }
            case 18: {
              eu.dnetlib.data.proto.DedupSimilarityProtos.DedupSimilarity.Builder subBuilder = eu.dnetlib.data.proto.DedupSimilarityProtos.DedupSimilarity.newBuilder();
              if (hasDedupSimilarity()) {
                subBuilder.mergeFrom(getDedupSimilarity());
              }
              input.readMessage(subBuilder, extensionRegistry);
              setDedupSimilarity(subBuilder.buildPartial());
              break;
            }
          }
        }
      }
      
      private int bitField0_;
      
      // optional .eu.dnetlib.data.proto.Dedup dedup = 1;
      private eu.dnetlib.data.proto.DedupProtos.Dedup dedup_ = eu.dnetlib.data.proto.DedupProtos.Dedup.getDefaultInstance();
      private com.google.protobuf.SingleFieldBuilder<
          eu.dnetlib.data.proto.DedupProtos.Dedup, eu.dnetlib.data.proto.DedupProtos.Dedup.Builder, eu.dnetlib.data.proto.DedupProtos.DedupOrBuilder> dedupBuilder_;
      public boolean hasDedup() {
        return ((bitField0_ & 0x00000001) == 0x00000001);
      }
      public eu.dnetlib.data.proto.DedupProtos.Dedup getDedup() {
        if (dedupBuilder_ == null) {
          return dedup_;
        } else {
          return dedupBuilder_.getMessage();
        }
      }
      public Builder setDedup(eu.dnetlib.data.proto.DedupProtos.Dedup value) {
        if (dedupBuilder_ == null) {
          if (value == null) {
            throw new NullPointerException();
          }
          dedup_ = value;
          onChanged();
        } else {
          dedupBuilder_.setMessage(value);
        }
        bitField0_ |= 0x00000001;
        return this;
      }
      public Builder setDedup(
          eu.dnetlib.data.proto.DedupProtos.Dedup.Builder builderForValue) {
        if (dedupBuilder_ == null) {
          dedup_ = builderForValue.build();
          onChanged();
        } else {
          dedupBuilder_.setMessage(builderForValue.build());
        }
        bitField0_ |= 0x00000001;
        return this;
      }
      public Builder mergeDedup(eu.dnetlib.data.proto.DedupProtos.Dedup value) {
        if (dedupBuilder_ == null) {
          if (((bitField0_ & 0x00000001) == 0x00000001) &&
              dedup_ != eu.dnetlib.data.proto.DedupProtos.Dedup.getDefaultInstance()) {
            dedup_ =
              eu.dnetlib.data.proto.DedupProtos.Dedup.newBuilder(dedup_).mergeFrom(value).buildPartial();
          } else {
            dedup_ = value;
          }
          onChanged();
        } else {
          dedupBuilder_.mergeFrom(value);
        }
        bitField0_ |= 0x00000001;
        return this;
      }
      public Builder clearDedup() {
        if (dedupBuilder_ == null) {
          dedup_ = eu.dnetlib.data.proto.DedupProtos.Dedup.getDefaultInstance();
          onChanged();
        } else {
          dedupBuilder_.clear();
        }
        bitField0_ = (bitField0_ & ~0x00000001);
        return this;
      }
      public eu.dnetlib.data.proto.DedupProtos.Dedup.Builder getDedupBuilder() {
        bitField0_ |= 0x00000001;
        onChanged();
        return getDedupFieldBuilder().getBuilder();
      }
      public eu.dnetlib.data.proto.DedupProtos.DedupOrBuilder getDedupOrBuilder() {
        if (dedupBuilder_ != null) {
          return dedupBuilder_.getMessageOrBuilder();
        } else {
          return dedup_;
        }
      }
      private com.google.protobuf.SingleFieldBuilder<
          eu.dnetlib.data.proto.DedupProtos.Dedup, eu.dnetlib.data.proto.DedupProtos.Dedup.Builder, eu.dnetlib.data.proto.DedupProtos.DedupOrBuilder> 
          getDedupFieldBuilder() {
        if (dedupBuilder_ == null) {
          dedupBuilder_ = new com.google.protobuf.SingleFieldBuilder<
              eu.dnetlib.data.proto.DedupProtos.Dedup, eu.dnetlib.data.proto.DedupProtos.Dedup.Builder, eu.dnetlib.data.proto.DedupProtos.DedupOrBuilder>(
                  dedup_,
                  getParentForChildren(),
                  isClean());
          dedup_ = null;
        }
        return dedupBuilder_;
      }
      
      // optional .eu.dnetlib.data.proto.DedupSimilarity dedupSimilarity = 2;
      private eu.dnetlib.data.proto.DedupSimilarityProtos.DedupSimilarity dedupSimilarity_ = eu.dnetlib.data.proto.DedupSimilarityProtos.DedupSimilarity.getDefaultInstance();
      private com.google.protobuf.SingleFieldBuilder<
          eu.dnetlib.data.proto.DedupSimilarityProtos.DedupSimilarity, eu.dnetlib.data.proto.DedupSimilarityProtos.DedupSimilarity.Builder, eu.dnetlib.data.proto.DedupSimilarityProtos.DedupSimilarityOrBuilder> dedupSimilarityBuilder_;
      public boolean hasDedupSimilarity() {
        return ((bitField0_ & 0x00000002) == 0x00000002);
      }
      public eu.dnetlib.data.proto.DedupSimilarityProtos.DedupSimilarity getDedupSimilarity() {
        if (dedupSimilarityBuilder_ == null) {
          return dedupSimilarity_;
        } else {
          return dedupSimilarityBuilder_.getMessage();
        }
      }
      public Builder setDedupSimilarity(eu.dnetlib.data.proto.DedupSimilarityProtos.DedupSimilarity value) {
        if (dedupSimilarityBuilder_ == null) {
          if (value == null) {
            throw new NullPointerException();
          }
          dedupSimilarity_ = value;
          onChanged();
        } else {
          dedupSimilarityBuilder_.setMessage(value);
        }
        bitField0_ |= 0x00000002;
        return this;
      }
      public Builder setDedupSimilarity(
          eu.dnetlib.data.proto.DedupSimilarityProtos.DedupSimilarity.Builder builderForValue) {
        if (dedupSimilarityBuilder_ == null) {
          dedupSimilarity_ = builderForValue.build();
          onChanged();
        } else {
          dedupSimilarityBuilder_.setMessage(builderForValue.build());
        }
        bitField0_ |= 0x00000002;
        return this;
      }
      public Builder mergeDedupSimilarity(eu.dnetlib.data.proto.DedupSimilarityProtos.DedupSimilarity value) {
        if (dedupSimilarityBuilder_ == null) {
          if (((bitField0_ & 0x00000002) == 0x00000002) &&
              dedupSimilarity_ != eu.dnetlib.data.proto.DedupSimilarityProtos.DedupSimilarity.getDefaultInstance()) {
            dedupSimilarity_ =
              eu.dnetlib.data.proto.DedupSimilarityProtos.DedupSimilarity.newBuilder(dedupSimilarity_).mergeFrom(value).buildPartial();
          } else {
            dedupSimilarity_ = value;
          }
          onChanged();
        } else {
          dedupSimilarityBuilder_.mergeFrom(value);
        }
        bitField0_ |= 0x00000002;
        return this;
      }
      public Builder clearDedupSimilarity() {
        if (dedupSimilarityBuilder_ == null) {
          dedupSimilarity_ = eu.dnetlib.data.proto.DedupSimilarityProtos.DedupSimilarity.getDefaultInstance();
          onChanged();
        } else {
          dedupSimilarityBuilder_.clear();
        }
        bitField0_ = (bitField0_ & ~0x00000002);
        return this;
      }
      public eu.dnetlib.data.proto.DedupSimilarityProtos.DedupSimilarity.Builder getDedupSimilarityBuilder() {
        bitField0_ |= 0x00000002;
        onChanged();
        return getDedupSimilarityFieldBuilder().getBuilder();
      }
      public eu.dnetlib.data.proto.DedupSimilarityProtos.DedupSimilarityOrBuilder getDedupSimilarityOrBuilder() {
        if (dedupSimilarityBuilder_ != null) {
          return dedupSimilarityBuilder_.getMessageOrBuilder();
        } else {
          return dedupSimilarity_;
        }
      }
      private com.google.protobuf.SingleFieldBuilder<
          eu.dnetlib.data.proto.DedupSimilarityProtos.DedupSimilarity, eu.dnetlib.data.proto.DedupSimilarityProtos.DedupSimilarity.Builder, eu.dnetlib.data.proto.DedupSimilarityProtos.DedupSimilarityOrBuilder> 
          getDedupSimilarityFieldBuilder() {
        if (dedupSimilarityBuilder_ == null) {
          dedupSimilarityBuilder_ = new com.google.protobuf.SingleFieldBuilder<
              eu.dnetlib.data.proto.DedupSimilarityProtos.DedupSimilarity, eu.dnetlib.data.proto.DedupSimilarityProtos.DedupSimilarity.Builder, eu.dnetlib.data.proto.DedupSimilarityProtos.DedupSimilarityOrBuilder>(
                  dedupSimilarity_,
                  getParentForChildren(),
                  isClean());
          dedupSimilarity_ = null;
        }
        return dedupSimilarityBuilder_;
      }
      
      // @@protoc_insertion_point(builder_scope:eu.dnetlib.data.proto.OrganizationOrganization)
    }
    
    static {
      defaultInstance = new OrganizationOrganization(true);
      defaultInstance.initFields();
    }
    
    // @@protoc_insertion_point(class_scope:eu.dnetlib.data.proto.OrganizationOrganization)
  }
  
  private static com.google.protobuf.Descriptors.Descriptor
    internal_static_eu_dnetlib_data_proto_OrganizationOrganization_descriptor;
  private static
    com.google.protobuf.GeneratedMessage.FieldAccessorTable
      internal_static_eu_dnetlib_data_proto_OrganizationOrganization_fieldAccessorTable;
  
  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\037Organization_Organization.proto\022\025eu.dn" +
      "etlib.data.proto\032\021RelMetadata.proto\032\013Ded" +
      "up.proto\032\025DedupSimilarity.proto\"\210\001\n\030Orga" +
      "nizationOrganization\022+\n\005dedup\030\001 \001(\0132\034.eu" +
      ".dnetlib.data.proto.Dedup\022?\n\017dedupSimila" +
      "rity\030\002 \001(\0132&.eu.dnetlib.data.proto.Dedup" +
      "SimilarityB7\n\025eu.dnetlib.data.protoB\036Org" +
      "anizationOrganizationProtos"
    };
    com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner =
      new com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner() {
        public com.google.protobuf.ExtensionRegistry assignDescriptors(
            com.google.protobuf.Descriptors.FileDescriptor root) {
          descriptor = root;
          internal_static_eu_dnetlib_data_proto_OrganizationOrganization_descriptor =
            getDescriptor().getMessageTypes().get(0);
          internal_static_eu_dnetlib_data_proto_OrganizationOrganization_fieldAccessorTable = new
            com.google.protobuf.GeneratedMessage.FieldAccessorTable(
              internal_static_eu_dnetlib_data_proto_OrganizationOrganization_descriptor,
              new java.lang.String[] { "Dedup", "DedupSimilarity", },
              eu.dnetlib.data.proto.OrganizationOrganizationProtos.OrganizationOrganization.class,
              eu.dnetlib.data.proto.OrganizationOrganizationProtos.OrganizationOrganization.Builder.class);
          return null;
        }
      };
    com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
          eu.dnetlib.data.proto.RelMetadataProtos.getDescriptor(),
          eu.dnetlib.data.proto.DedupProtos.getDescriptor(),
          eu.dnetlib.data.proto.DedupSimilarityProtos.getDescriptor(),
        }, assigner);
  }
  
  // @@protoc_insertion_point(outer_class_scope)
}
