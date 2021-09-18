package test.utils;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.google.crypto.tink.Aead;
import com.google.crypto.tink.CleartextKeysetHandle;
import com.google.crypto.tink.JsonKeysetReader;
import com.google.crypto.tink.JsonKeysetWriter;
import com.google.crypto.tink.KeyTemplates;
import com.google.crypto.tink.KeysetHandle;
import com.google.crypto.tink.Mac;
import com.google.crypto.tink.Registry;
import com.google.crypto.tink.aead.AeadConfig;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EncryptUtils {

    public void test() throws GeneralSecurityException, IOException {
        AeadConfig.register();
        
        List<String> keyTemplates = Registry.keyTemplates();
        log.info("keyTemplates = {}", StringUtils.join(keyTemplates, ","));
        
        KeysetHandle keysetHandle = KeysetHandle.generateNew(KeyTemplates.get("CHACHA20_POLY1305"));

        Aead aead = keysetHandle.getPrimitive(Aead.class);
        
        
        byte[] associatedData = null;
        byte[] encrypt = aead.encrypt("1234567890".getBytes(), associatedData);
        log.info("encrypt = {}", new String(encrypt));
        byte[] decrypt = aead.decrypt(encrypt, associatedData);
        log.info("decrypt = {}", new String(decrypt));
    }
    
    public void test1() throws GeneralSecurityException, IOException {
        
        KeysetHandle keysetHandle = KeysetHandle.generateNew(KeyTemplates.get("CHACHA20_POLY1305"));
        
        CleartextKeysetHandle.write(keysetHandle, JsonKeysetWriter.withFile(new File("")));
        
        KeysetHandle keysetHandle2 = CleartextKeysetHandle.read(JsonKeysetReader.withFile(new File("")));
        
        Mac mac = keysetHandle.getPrimitive(Mac.class);
    }
    
    public static void main(String[] args) throws Exception {
        new EncryptUtils().test();
    }
}
