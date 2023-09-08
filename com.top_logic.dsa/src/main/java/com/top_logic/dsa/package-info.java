/**
 * Base package for {@link com.top_logic.dsa.DataSourceAdaptor},
 * {@link com.top_logic.dsa.DataAccessProxy} and the {@link com.top_logic.dsa.DataAccessService}.
 * <p>
 * {@link com.top_logic.dsa.DataSourceAdaptor}s allow stream based or structured access to
 * hierarchical data. The data may optionally be versioned.
 * {@link com.top_logic.dsa.DataAccessProxy} hides the possible (multiple) implementations of
 * {@link com.top_logic.dsa.DataSourceAdaptor}s behind a common access method. The
 * {@link com.top_logic.dsa.DataAccessService} manages all
 * {@link com.top_logic.dsa.DataSourceAdaptor}s and cares for their setup and teardown.
 * </p>
 */
package com.top_logic.dsa;
