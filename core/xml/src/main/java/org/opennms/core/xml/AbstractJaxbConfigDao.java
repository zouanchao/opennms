/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.core.xml;


import org.opennms.core.config.api.ConfigReloadContainer;
import org.opennms.core.config.api.ConfigurationProvider;
import org.opennms.core.config.api.ReloadableFile;
import org.opennms.core.config.api.ReloadingContainer;
import org.opennms.core.spring.ReloadableResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
/**
 * <p>Abstract AbstractJaxbConfigDao class.</p>
 *
 * @author <a href="mailto:dj@gregor.com">DJ Gregor</a>
 * @param <K> JAXB class
 * @param <V> Configuration object that is stored in memory (might be the same
 *            as the JAXB class or could be a different class)
 * @version $Id: $
 */
public abstract class AbstractJaxbConfigDao<K, V> implements InitializingBean {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractJaxbConfigDao.class);

    private Class<K> m_jaxbClass;
    private String m_description;
    private Resource m_configResource;
    private ReloadingContainer<V> m_container;
    private Long m_reloadCheckInterval = null;

    /**
     * <p>Constructor for AbstractJaxbConfigDao.</p>
     *
     * @param entityClass a {@link java.lang.Class} object.
     * @param description a {@link java.lang.String} object.
     */
    public AbstractJaxbConfigDao(final Class<K> entityClass, final String description) {
        super();

        m_jaxbClass = entityClass;
        m_description = description;
    }

    /**
     * <p>translateConfig</p>
     *
     * @param config a K object.
     * @return a V object.
     */
    protected abstract V translateConfig(K config);

    protected V loadConfig(final byte[] bytes) {
        long startTime = System.currentTimeMillis();

        LOG.debug("Loading {} configuration.", m_description);

        V config = translateConfig(JaxbUtils.unmarshal(m_jaxbClass, new String(bytes)));

        long endTime = System.currentTimeMillis();

        LOG.info("Loaded {} in {} ms", getDescription(), (endTime - startTime));

        return config;
    }

    protected V mergeConfigs(V source, V target) {
        return source;
    }

    /**
     * <p>afterPropertiesSet</p>
     */
    @Override
    public void afterPropertiesSet() {
        Assert.state(m_configResource != null, "property configResource must be set and be non-null");

        final V config;
        final byte[] bytes;
        try (InputStream is = m_configResource.getInputStream()){
            bytes = StreamUtils.copyToByteArray(is);
            config = loadConfig(bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        ConfigurationProvider provider;
        try {
            provider = new ReloadableFile(m_configResource.getFile());
        } catch (final IOException e) {
            LOG.info("Resource '{}' does not seem to have an underlying File object; assuming this is not an auto-reloadable file resource", m_configResource);
            provider = new ReloadableResource(m_configResource, bytes);
        }

        m_container = new ConfigReloadContainer.Builder<V>()
                .withInitialConfig(config)
                .withProvider(provider)
                .withLoader(this::loadConfig)
                .withMerger(this::mergeConfigs)
                .withReloadCheckInterval(m_reloadCheckInterval)
                .withExtensionType(m_jaxbClass.getCanonicalName())
                .build();
    }

    /**
     * <p>getConfigResource</p>
     *
     * @return a {@link org.springframework.core.io.Resource} object.
     */
    public Resource getConfigResource() {
        return m_configResource;
    }

    /**
     * <p>setConfigResource</p>
     *
     * @param configResource a {@link org.springframework.core.io.Resource} object.
     */
    public void setConfigResource(final Resource configResource) {
        m_configResource = configResource;
    }

    public ReloadingContainer<V> getContainer() {
        return m_container;
    }

    /**
     * <p>getReloadCheckInterval</p>
     *
     * @return a {@link java.lang.Long} object.
     */
    public Long getReloadCheckInterval() {
        return m_reloadCheckInterval;
    }

    /**
     * <p>setReloadCheckInterval</p>
     *
     * @param reloadCheckInterval a {@link java.lang.Long} object.
     */
    public void setReloadCheckInterval(final Long reloadCheckInterval) {
        m_reloadCheckInterval = reloadCheckInterval;
        if (m_reloadCheckInterval != null && m_container != null) {
            m_container.setReloadCheckInterval(m_reloadCheckInterval);
        }
    }

    /**
     * <p>getDescription</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getDescription() {
        return m_description;
    }
}
