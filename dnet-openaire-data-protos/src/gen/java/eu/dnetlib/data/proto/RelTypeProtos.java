// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: RelType.proto

package eu.dnetlib.data.proto;

public final class RelTypeProtos {
  private RelTypeProtos() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
  }
  public enum RelType
      implements com.google.protobuf.ProtocolMessageEnum {
    datasourceOrganization(0, 1),
    projectOrganization(1, 4),
    resultOrganization(2, 5),
    resultProject(3, 6),
    resultResult(4, 9),
    organizationOrganization(5, 11),
    ;
    
    public static final int datasourceOrganization_VALUE = 1;
    public static final int projectOrganization_VALUE = 4;
    public static final int resultOrganization_VALUE = 5;
    public static final int resultProject_VALUE = 6;
    public static final int resultResult_VALUE = 9;
    public static final int organizationOrganization_VALUE = 11;
    
    
    public final int getNumber() { return value; }
    
    public static RelType valueOf(int value) {
      switch (value) {
        case 1: return datasourceOrganization;
        case 4: return projectOrganization;
        case 5: return resultOrganization;
        case 6: return resultProject;
        case 9: return resultResult;
        case 11: return organizationOrganization;
        default: return null;
      }
    }
    
    public static com.google.protobuf.Internal.EnumLiteMap<RelType>
        internalGetValueMap() {
      return internalValueMap;
    }
    private static com.google.protobuf.Internal.EnumLiteMap<RelType>
        internalValueMap =
          new com.google.protobuf.Internal.EnumLiteMap<RelType>() {
            public RelType findValueByNumber(int number) {
              return RelType.valueOf(number);
            }
          };
    
    public final com.google.protobuf.Descriptors.EnumValueDescriptor
        getValueDescriptor() {
      return getDescriptor().getValues().get(index);
    }
    public final com.google.protobuf.Descriptors.EnumDescriptor
        getDescriptorForType() {
      return getDescriptor();
    }
    public static final com.google.protobuf.Descriptors.EnumDescriptor
        getDescriptor() {
      return eu.dnetlib.data.proto.RelTypeProtos.getDescriptor().getEnumTypes().get(0);
    }
    
    private static final RelType[] VALUES = {
      datasourceOrganization, projectOrganization, resultOrganization, resultProject, resultResult, organizationOrganization, 
    };
    
    public static RelType valueOf(
        com.google.protobuf.Descriptors.EnumValueDescriptor desc) {
      if (desc.getType() != getDescriptor()) {
        throw new java.lang.IllegalArgumentException(
          "EnumValueDescriptor is not for this type.");
      }
      return VALUES[desc.getIndex()];
    }
    
    private final int index;
    private final int value;
    
    private RelType(int index, int value) {
      this.index = index;
      this.value = value;
    }
    
    // @@protoc_insertion_point(enum_scope:eu.dnetlib.data.proto.RelType)
  }
  
  public enum SubRelType
      implements com.google.protobuf.ProtocolMessageEnum {
    provision(0, 1),
    participation(1, 4),
    outcome(2, 6),
    similarity(3, 8),
    publicationDataset(4, 9),
    affiliation(5, 12),
    dedup(6, 10),
    dedupSimilarity(7, 11),
    supplement(8, 13),
    part(9, 15),
    version(10, 16),
    relationship(11, 17),
    ;
    
    public static final int provision_VALUE = 1;
    public static final int participation_VALUE = 4;
    public static final int outcome_VALUE = 6;
    public static final int similarity_VALUE = 8;
    public static final int publicationDataset_VALUE = 9;
    public static final int affiliation_VALUE = 12;
    public static final int dedup_VALUE = 10;
    public static final int dedupSimilarity_VALUE = 11;
    public static final int supplement_VALUE = 13;
    public static final int part_VALUE = 15;
    public static final int version_VALUE = 16;
    public static final int relationship_VALUE = 17;
    
    
    public final int getNumber() { return value; }
    
    public static SubRelType valueOf(int value) {
      switch (value) {
        case 1: return provision;
        case 4: return participation;
        case 6: return outcome;
        case 8: return similarity;
        case 9: return publicationDataset;
        case 12: return affiliation;
        case 10: return dedup;
        case 11: return dedupSimilarity;
        case 13: return supplement;
        case 15: return part;
        case 16: return version;
        case 17: return relationship;
        default: return null;
      }
    }
    
    public static com.google.protobuf.Internal.EnumLiteMap<SubRelType>
        internalGetValueMap() {
      return internalValueMap;
    }
    private static com.google.protobuf.Internal.EnumLiteMap<SubRelType>
        internalValueMap =
          new com.google.protobuf.Internal.EnumLiteMap<SubRelType>() {
            public SubRelType findValueByNumber(int number) {
              return SubRelType.valueOf(number);
            }
          };
    
    public final com.google.protobuf.Descriptors.EnumValueDescriptor
        getValueDescriptor() {
      return getDescriptor().getValues().get(index);
    }
    public final com.google.protobuf.Descriptors.EnumDescriptor
        getDescriptorForType() {
      return getDescriptor();
    }
    public static final com.google.protobuf.Descriptors.EnumDescriptor
        getDescriptor() {
      return eu.dnetlib.data.proto.RelTypeProtos.getDescriptor().getEnumTypes().get(1);
    }
    
    private static final SubRelType[] VALUES = {
      provision, participation, outcome, similarity, publicationDataset, affiliation, dedup, dedupSimilarity, supplement, part, version, relationship, 
    };
    
    public static SubRelType valueOf(
        com.google.protobuf.Descriptors.EnumValueDescriptor desc) {
      if (desc.getType() != getDescriptor()) {
        throw new java.lang.IllegalArgumentException(
          "EnumValueDescriptor is not for this type.");
      }
      return VALUES[desc.getIndex()];
    }
    
    private final int index;
    private final int value;
    
    private SubRelType(int index, int value) {
      this.index = index;
      this.value = value;
    }
    
    // @@protoc_insertion_point(enum_scope:eu.dnetlib.data.proto.SubRelType)
  }
  
  
  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\rRelType.proto\022\025eu.dnetlib.data.proto*\231" +
      "\001\n\007RelType\022\032\n\026datasourceOrganization\020\001\022\027" +
      "\n\023projectOrganization\020\004\022\026\n\022resultOrganiz" +
      "ation\020\005\022\021\n\rresultProject\020\006\022\020\n\014resultResu" +
      "lt\020\t\022\034\n\030organizationOrganization\020\013*\315\001\n\nS" +
      "ubRelType\022\r\n\tprovision\020\001\022\021\n\rparticipatio" +
      "n\020\004\022\013\n\007outcome\020\006\022\016\n\nsimilarity\020\010\022\026\n\022publ" +
      "icationDataset\020\t\022\017\n\013affiliation\020\014\022\t\n\005ded" +
      "up\020\n\022\023\n\017dedupSimilarity\020\013\022\016\n\nsupplement\020" +
      "\r\022\010\n\004part\020\017\022\013\n\007version\020\020\022\020\n\014relationship",
      "\020\021B&\n\025eu.dnetlib.data.protoB\rRelTypeProt" +
      "os"
    };
    com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner =
      new com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner() {
        public com.google.protobuf.ExtensionRegistry assignDescriptors(
            com.google.protobuf.Descriptors.FileDescriptor root) {
          descriptor = root;
          return null;
        }
      };
    com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
        }, assigner);
  }
  
  // @@protoc_insertion_point(outer_class_scope)
}
