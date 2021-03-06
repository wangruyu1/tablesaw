package tech.tablesaw;

import org.junit.Before;
import org.junit.Test;

import tech.tablesaw.api.CategoryColumn;
import tech.tablesaw.api.ColumnType;
import tech.tablesaw.api.Table;
import tech.tablesaw.io.csv.CsvReadOptions;
import tech.tablesaw.table.SubTable;
import tech.tablesaw.table.TableGroup;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Tests grouping and aggregation operations on tables
 */
public class TableGroupTest {

    private static ColumnType[] types = {
            ColumnType.LOCAL_DATE,     // date of poll
            ColumnType.INTEGER,        // approval rating (pct)
            ColumnType.CATEGORY        // polling org
    };

    private Table table;

    @Before
    public void setUp() throws Exception {
        table = Table.read().csv(CsvReadOptions.builder("../data/BushApproval.csv").columnTypes(types));
    }

    @Test
    public void testGetSubTables() {
        TableGroup tableGroup = new TableGroup(table, table.column("who"));
        List<SubTable> tables = tableGroup.getSubTables();
        assertEquals(6, tables.size());
    }

    @Test
    public void testWith2GroupingCols() {
        CategoryColumn month = table.dateColumn(0).month();
        month.setName("month");
        table.addColumn(month);
        String[] splitColumnNames = {table.column(2).name(), "month"};
        TableGroup tableGroup = new TableGroup(table, splitColumnNames);
        List<SubTable> tables = tableGroup.getSubTables();
        Table t = table.sum("approval").by(splitColumnNames);

        // compare the sum of the original column with the sum of the sums of the group table
        assertEquals(table.intColumn(1).sum(), Math.round(t.floatColumn(2).sum()));
        assertEquals(65, tables.size());
    }

    @Test
    public void testCountByGroup() {
        Table groups = table.count("approval").by("who");
        assertEquals(2, groups.columnCount());
        assertEquals(6, groups.rowCount());
        CategoryColumn group = groups.categoryColumn(0);
        assertTrue(group.contains("fox"));
    }

    @Test
    public void testSumGroup() {
        Table groups = table.sum("approval").by("who");
        // compare the sum of the original column with the sum of the sums of the group table
        assertEquals(table.intColumn(1).sum(), Math.round(groups.floatColumn(1).sum()));
    }
}