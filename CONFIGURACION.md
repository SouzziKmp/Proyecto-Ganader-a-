# Guía de configuración — Proyecto Java + Oracle ADB (Wallet)

## 1. Estructura del proyecto

```
GanaderiaADB/
├── src/
│   └── ganaderia/
│       ├── Main.java
│       ├── db/
│       │   └── ConexionADB.java
│       ├── dao/
│       │   ├── AnimalDAO.java
│       │   ├── ProduccionVentasDAO.java
│       │   └── UsuarioDAO.java
│       └── modelo/
│           └── Animal.java
├── lib/
│   ├── ojdbc11.jar
│   ├── oraclepki.jar
│   ├── osdt_core.jar
│   └── osdt_cert.jar
├── wallet/          ← Aquí van los archivos del Wallet descomprimido
│   ├── tnsnames.ora
│   ├── sqlnet.ora
│   ├── cwallet.sso
│   ├── ewallet.p12
│   ├── keystore.jks
│   └── truststore.jks
└── pom.xml          ← Si usas Maven
```

---

## 2. Descargar los drivers JDBC

Ve a: https://www.oracle.com/database/technologies/appdev/jdbc-downloads.html

Descarga el ZIP de **"21c / 23ai JDBC and Companion Jars"** y extrae:
- `ojdbc11.jar`
- `oraclepki.jar`
- `osdt_core.jar`
- `osdt_cert.jar`

Colócalos en la carpeta `lib/`.

---

## 3. Descargar el Wallet desde OCI

1. OCI Console → tu ADB → clic en **"DB Connection"**
2. Clic en **"Download Wallet"**
3. Asigna una contraseña al wallet (guárdala, la necesitarás)
4. Descomprime el ZIP en la carpeta `wallet/`

---

## 4. Configurar ConexionADB.java

Edita las constantes al inicio de `ConexionADB.java`:

```java
private static final String WALLET_PATH     = "C:/GanaderiaADB/wallet";
private static final String WALLET_PASSWORD = "TuPasswordDelWallet";
private static final String DB_USER         = "ADMIN";
private static final String DB_PASSWORD     = "TuPasswordDeAdmin";
private static final String TNS_ALIAS       = "ganaderia_high";
```

El alias (`ganaderia_high`, `ganaderia_medium`, etc.) lo encuentras
dentro del archivo `tnsnames.ora` del wallet. Abre ese archivo con
el Bloc de Notas y verás algo como:

```
ganaderia_high = (description= (retry_count=20)...)
ganaderia_medium = (description= ...)
ganaderia_low = (description= ...)
```

Elige `_high` para la aplicación principal (más recursos).

---

## 5. Compilar y ejecutar (sin Maven)

```bash
# Compilar (Windows — separa con ; en Linux usa :)
javac -cp "lib/*" -d out src/ganaderia/db/ConexionADB.java
javac -cp "lib/*;out" -d out src/ganaderia/modelo/Animal.java
javac -cp "lib/*;out" -d out src/ganaderia/dao/*.java
javac -cp "lib/*;out" -d out src/ganaderia/Main.java

# Ejecutar
java -cp "lib/*;out" ganaderia.Main
```

---

## 6. pom.xml (si usas Maven + IntelliJ/Eclipse)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>cr.universidad</groupId>
    <artifactId>GanaderiaADB</artifactId>
    <version>1.0</version>
    <packaging>jar</packaging>

    <dependencies>
        <!-- Driver Oracle JDBC -->
        <dependency>
            <groupId>com.oracle.database.jdbc</groupId>
            <artifactId>ojdbc11</artifactId>
            <version>23.4.0.24.05</version>
        </dependency>
        <!-- PKI para Wallet -->
        <dependency>
            <groupId>com.oracle.database.security</groupId>
            <artifactId>oraclepki</artifactId>
            <version>23.4.0.24.05</version>
        </dependency>
        <dependency>
            <groupId>com.oracle.database.security</groupId>
            <artifactId>osdt_core</artifactId>
            <version>21.13.0.0</version>
        </dependency>
        <dependency>
            <groupId>com.oracle.database.security</groupId>
            <artifactId>osdt_cert</artifactId>
            <version>21.13.0.0</version>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <configuration>
                    <source>17</source>
                    <target>17</target>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

---

## 7. Cómo llamar un Store Procedure desde Java

### SP sin valor de retorno (solo OUT de confirmación)
```java
CallableStatement cs = con.prepareCall("{ CALL SP_INGRESA_USUARIO(?,?,?,?,?) }");
cs.setString(1, nombre);
cs.setString(2, username);
cs.setString(3, hash);
cs.setString(4, rol);
cs.registerOutParameter(5, Types.VARCHAR);
cs.execute();
String resultado = cs.getString(5);
con.commit();
```

### Función PL/SQL (retorna un valor)
```java
CallableStatement cs = con.prepareCall("{ ? = CALL FN_VALIDAR_ARETE(?) }");
cs.registerOutParameter(1, Types.VARCHAR);
cs.setString(2, arete);
cs.execute();
String valido = cs.getString(1);
```

### Cursor (lista de registros)
```java
CallableStatement cs = con.prepareCall("BEGIN OPEN ? FOR SELECT ... END;");
cs.registerOutParameter(1, oracle.jdbc.OracleTypes.CURSOR);
cs.execute();
ResultSet rs = (ResultSet) cs.getObject(1);
while (rs.next()) { ... }
rs.close();
```

---

## 8. Errores comunes y soluciones

| Error | Solución |
|-------|----------|
| `IO Error: The Network Adapter could not establish the connection` | Revisa que el alias del TNS_ALIAS sea exacto al del `tnsnames.ora` |
| `PKIException: Unable to create keystore` | Verifica que `WALLET_PATH` apunta a la carpeta correcta con los `.jks` |
| `ORA-01017: invalid username/password` | Revisa `DB_USER` y `DB_PASSWORD` en `ConexionADB.java` |
| `ClassNotFoundException: oracle.jdbc.OracleDriver` | Falta el `.jar` en el classpath |
| `ORA-06550: PLS-00302` | El nombre del procedure/función está mal escrito o no existe |
