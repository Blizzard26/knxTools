# Get ETS5 XSD
1. Download and install ETS5
2. In the installation directory locate File `Knx.Ets.Xml.ObjectModel.dll`. Open the file in a UTF-8 capable editor (e.g., Notepad++). You'll see a lot of garbage - don't worry.
3. In the file search for `<xs:schema`. Mark and copy anything from `<xs:schema` to `</xs:schema>` (including both). Past it to a new text file (UTF-8) and save the file as `knx.xsd` in this directory.

# Patch ETS5 XSD
Line 3-5 replace
```xml
  <xs:simpleType name="IDREF">
    <xs:restriction base="xs:NCName"/>
  </xs:simpleType>
```
by
```xml
  <xs:simpleType name="IDREF">
    <xs:restriction base="xs:IDREF"/>
  </xs:simpleType>
```

Line 9-11 replace
```xml
  <xs:simpleType name="RELIDREF">
    <xs:restriction base="xs:NCName"/>
  </xs:simpleType>
```
by
```xml
  <xs:simpleType name="RELIDREF">
    <xs:restriction base="xs:IDREF"/>
  </xs:simpleType>
```

Line 15-17 replace
```xml
  <xs:simpleType name="RELID">
    <xs:restriction base="xs:NCName"/>
  </xs:simpleType>
```
by
```xml
  <xs:simpleType name="RELID">
    <xs:restriction base="xs:ID"/>
  </xs:simpleType>
```

Lines 5581, 5582, and 5599 replace `maxOccurs="65535"` by `maxOccurs="unbounded"`