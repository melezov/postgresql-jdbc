/*-------------------------------------------------------------------------
*
* Copyright (c) 2005, PostgreSQL Global Development Group
*
* IDENTIFICATION
*   $PostgreSQL: pgjdbc/org/postgresql/test/jdbc3/ParameterMetaDataTest.java,v 1.1 2005/02/01 07:27:55 jurka Exp $
*
*-------------------------------------------------------------------------
*/
package org.postgresql.test.jdbc3;

import java.sql.*;
import junit.framework.TestCase;
import org.postgresql.test.TestUtil;

public class ParameterMetaDataTest extends TestCase {

    private Connection _conn;

    public ParameterMetaDataTest(String name) {
        super(name);
    }

    protected void setUp() throws SQLException {
        _conn = TestUtil.openDB();
        TestUtil.createTable(_conn, "parametertest", "a int4, b float8, c text");
    }

    protected void tearDown() throws SQLException {
        TestUtil.dropTable(_conn, "parametertest");
        TestUtil.closeDB(_conn);
    }

    public void testParameterMD() throws SQLException {
        if (!TestUtil.haveMinimumServerVersion(_conn, "7.4"))
            return;

        PreparedStatement pstmt = _conn.prepareStatement("SELECT a FROM parametertest WHERE b = ? AND c = ?");
        ParameterMetaData pmd = pstmt.getParameterMetaData();

        assertEquals(2, pmd.getParameterCount());
        assertEquals(Types.DOUBLE, pmd.getParameterType(1));
        assertEquals("float8", pmd.getParameterTypeName(1));
        assertEquals(Types.VARCHAR, pmd.getParameterType(2));
        assertEquals("text", pmd.getParameterTypeName(2));

        pstmt.close();
    }

    public void testFailsOnBadIndex() throws SQLException {
        if (!TestUtil.haveMinimumServerVersion(_conn, "7.4"))
            return;

        PreparedStatement pstmt = _conn.prepareStatement("SELECT a FROM parametertest WHERE b = ? AND c = ?");
        ParameterMetaData pmd = pstmt.getParameterMetaData();
        try {
            pmd.getParameterType(0);
            fail("Can't get parameter for index < 1.");
        } catch (SQLException sqle) { }
        try {
            pmd.getParameterType(3);
            fail("Can't get parameter for index 3 with only two parameters.");
        } catch (SQLException sqle) { }
    }

    // Make sure we work when mashing two queries into a single statement.
    public void testMultiStatement() throws SQLException {
        if (!TestUtil.haveMinimumServerVersion(_conn, "7.4"))
            return;

        PreparedStatement pstmt = _conn.prepareStatement("SELECT a FROM parametertest WHERE b = ? AND c = ? ; SELECT b FROM parametertest WHERE a = ?");
        ParameterMetaData pmd = pstmt.getParameterMetaData();

        assertEquals(3, pmd.getParameterCount());
        assertEquals(Types.DOUBLE, pmd.getParameterType(1));
        assertEquals("float8", pmd.getParameterTypeName(1));
        assertEquals(Types.VARCHAR, pmd.getParameterType(2));
        assertEquals("text", pmd.getParameterTypeName(2));
        assertEquals(Types.INTEGER, pmd.getParameterType(3));
        assertEquals("int4", pmd.getParameterTypeName(3));

        pstmt.close();

    }
}