package de.adorsys.keymanagement.core.collection.keystore;

import com.googlecode.cqengine.attribute.Attribute;
import de.adorsys.keymanagement.api.Queryable;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

import static com.googlecode.cqengine.query.QueryFactory.attribute;
import static com.googlecode.cqengine.query.QueryFactory.nullableAttribute;

@Getter
@Builder
@EqualsAndHashCode(of = "alias")
@RequiredArgsConstructor
public class QueryableKey implements Queryable {

    public static final Attribute<QueryableKey, String> ID = attribute("id", QueryableKey::getAlias);
    public static final Attribute<QueryableKey, KeyMetadata> META = attribute("meta", QueryableKey::getMetadata);
    public static final Attribute<QueryableKey, Boolean> IS_TRUST_CERT = attribute("is_trust_cert", it -> it.getKey() instanceof KeyStore.TrustedCertificateEntry);
    public static final Attribute<QueryableKey, Boolean> IS_SECRET = attribute("is_secret", it -> it.getKey() instanceof KeyStore.SecretKeyEntry);
    public static final Attribute<QueryableKey, Boolean> IS_PRIVATE = attribute("is_private", it -> it.getKey() instanceof KeyStore.PrivateKeyEntry);
    public static final Attribute<QueryableKey, Certificate> CERT = nullableAttribute(
            "cert", it -> {
                if (!(it.getKey() instanceof KeyStore.PrivateKeyEntry)) {
                    return null;
                }
                KeyStore.PrivateKeyEntry pKey = (KeyStore.PrivateKeyEntry) it.getKey();
                return pKey.getCertificate();
            });

    public static final Attribute<QueryableKey, Boolean> HAS_VALID_CERTS = attribute(
            "has_valid_certs", it -> {
                if (!(it.getKey() instanceof KeyStore.PrivateKeyEntry)) {
                    return false;
                }
                KeyStore.PrivateKeyEntry pKey = (KeyStore.PrivateKeyEntry) it.getKey();
                if (! (pKey.getCertificate() instanceof X509Certificate)) {
                    return false;
                }

                try {
                    ((X509Certificate) pKey.getCertificate()).checkValidity();
                } catch (Exception ex) {
                    return false;
                }

                return true;
            });

    private final String alias;
    private final KeyMetadata metadata;
    private final KeyStore.Entry key;

    QueryableKey(String alias, KeyStore.Entry entry) {
        this.alias = alias;
        this.metadata = null;
        this.key = entry;
    }
}