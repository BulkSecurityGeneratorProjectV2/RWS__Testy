package com.sdl.selenium.extjs6.tree;

import com.sdl.selenium.extjs6.grid.Cell;
import com.sdl.selenium.extjs6.grid.Scrollable;
import com.sdl.selenium.web.SearchType;
import com.sdl.selenium.web.WebLocator;
import com.sdl.selenium.web.table.AbstractCell;
import com.sdl.selenium.web.table.Table;
import com.sdl.selenium.web.utils.RetryUtils;
import com.sdl.selenium.web.utils.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriverException;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public class Tree extends WebLocator implements Scrollable {
    private static final Logger log = LogManager.getLogger(Tree.class);

    public Tree() {
        setClassName("Tree");
        setBaseCls("x-tree-panel");
    }

    public Tree(WebLocator container) {
        this();
        setContainer(container);
    }

    @Deprecated
    public boolean select(String... nodes) {
        return select(false, nodes);
    }

    public boolean select(List<String> nodes) {
        return select(false, nodes);
    }

    @Deprecated
    public boolean select(boolean doScroll, String... nodes) {
        return select(doScroll, List.of(nodes), SearchType.EQUALS, SearchType.TRIM);
    }

    public boolean select(List<String> nodes, SearchType... searchTypes) {
        return select(false, nodes, searchTypes);
    }

    public boolean select(boolean doScroll, List<String> nodes, SearchType... searchTypes) {
        if (doScroll) {
            scrollTop();
        }
        Table previousNodeEl = null;
        boolean selected = false;
        for (int i = 0; i < nodes.size(); i++) {
            String node = nodes.get(i);
            WebLocator textEl = new WebLocator().setText(node, searchTypes);
            WebLocator container = previousNodeEl == null ? this : previousNodeEl;
            Table nodeEl = new Table(container).setClasses("x-grid-item").setChildNodes(textEl).setVisibility(true);
            if (previousNodeEl != null) {
                nodeEl.setRoot("/following-sibling::");
            }
            com.sdl.selenium.web.table.Row row = nodeEl.getRow(1).setClasses("x-grid-row");
            boolean isExpanded;
            String aClass = row.getAttributeClass();
            isExpanded = aClass != null && aClass.contains("x-grid-tree-node-expanded");
            if (doScroll) {
                scrollPageDownTo(nodeEl);
            }
            WebLocator expanderEl = new WebLocator(nodeEl).setClasses("x-tree-expander");
            if (nodeEl.ready()) {
                if (!(isExpanded || aClass.contains("x-grid-tree-node-leaf"))) {
                    RetryUtils.retry(2, () -> {
                        expanderEl.click();
                        String aCls = row.getAttributeClass();
                        boolean contains = aCls.contains("x-grid-tree-node-expanded");
                        if (!contains) {
                            Utils.sleep(1);
                            log.error("Node '{}' is not expanded!!!", node);
                        } else {
                            log.info("Node '{}' is expanded.", node);
                        }
                        return contains;
                    });
                } else {
                    WebLocator checkTree = new WebLocator(nodeEl).setClasses("x-tree-checkbox");
                    WebLocator nodeTree = new WebLocator(nodeEl).setClasses("x-tree-node-text");
                    int nodeCount = nodeTree.size();
                    if (nodeCount > 1) {
                        WebLocator precedingSibling = new WebLocator(nodeTree).setTag("preceding-sibling::*").setClasses("x-tree-elbow-img");
                        for (int j = 1; j <= nodeCount; j++) {
                            nodeTree.setResultIdx(j);
                            int size = precedingSibling.size();
                            if (size == i + 1) {
                                break;
                            }
                        }
                    }
                    try {
                        if (checkTree.isPresent()) {
                            selected = checkTree.click();
                        } else {
                            selected = RetryUtils.retry(2, nodeTree::click);
                        }
                    } catch (WebDriverException e) {
                        if (doScroll) {
                            scrollPageDown();
                        }
                        if (checkTree.isPresent()) {
                            selected = checkTree.click();
                        } else {
                            selected = RetryUtils.retry(2, nodeTree::click);
                        }
                    }
                }
            }
            previousNodeEl = nodeEl;
        }
        return selected;
    }

    public boolean isSelected(String node) {
        WebLocator nodeEl = new WebLocator().setText(node);
        Table nodeSelected = new Table(this).setClasses("x-grid-item", "x-grid-item-selected").setChildNodes(nodeEl).setVisibility(true);
        return nodeSelected.isPresent();
    }

    public boolean isSelected(List<String> nodes) {
        Table previousNodeEl = null;
        Table nodeEl = null;
        int count = 0;
        for (String node : nodes) {
            WebLocator textEl = new WebLocator().setText(node, SearchType.EQUALS);
            WebLocator container = previousNodeEl == null ? this : previousNodeEl;
            nodeEl = new Table(container).setClasses("x-grid-item").setChildNodes(textEl).setVisibility(true);
            if (previousNodeEl != null) {
                nodeEl.setRoot("/following-sibling::");
            }
            previousNodeEl = nodeEl;
            count++;
        }
        int nodeCount = nodeEl.size();
        if (nodeCount > 1) {
            WebLocator nodeTree = new WebLocator(nodeEl).setClasses("x-tree-node-text");
            WebLocator precedingSibling = new WebLocator(nodeTree).setTag("preceding-sibling::*").setClasses("x-tree-elbow-img");
            for (int j = 1; j <= nodeCount; j++) {
                nodeTree.setResultIdx(j);
                int size = precedingSibling.size();
                if (size == count) {
                    nodeEl.setResultIdx(j);
                    break;
                }
            }
        }
        String aClass = nodeEl.getAttributeClass();
        return aClass.contains("x-grid-item-selected");
    }

    public void expandAllNodes() {
        Row rowsEl = new Row(this).setTag("tr").setExcludeClasses("x-grid-tree-node-leaf", "x-grid-tree-node-expanded");
        int size;
        do {
            Row row = new Row(this).setTag("tr").setExcludeClasses("x-grid-tree-node-leaf", "x-grid-tree-node-expanded").setResultIdx(1);
            WebLocator expanderEl = new WebLocator(row).setClasses("x-tree-expander").setRender(Duration.ofSeconds(1));
            expanderEl.doClick();
            size = rowsEl.size();
            if (size == 0) {
                scrollPageDown();
                size = rowsEl.size();
                if (size == 0) {
                    scrollPageDown();
                    size = rowsEl.size();
                    if (size == 0) {
                        Utils.sleep(1);
                    }
                }
            }
        } while (size != 0);
    }

    public List<List<String>> getValues(int... excludedColumns) {
        Row rowEl = new Row(this, 1);
        Cell columnsEl = new Cell(rowEl);
        int columns = columnsEl.size();
        Row rowsEl = new Row(this).setTag("tr");
        int rows = rowsEl.size();
        final List<Integer> columnsList = getColumns(columns, excludedColumns);
        return getValues(rows, columnsList);
    }

    public List<List<String>> getNodesValues(List<String> nodes, int... excludedColumns) {
        select(nodes.toArray(new String[0]));
        Row rowEl = new Row(this, 1);
        Cell columnsEl = new Cell(rowEl);
        int columns = columnsEl.size();
        List<List<String>> listOfList = new ArrayList<>();
        for (String node : nodes) {
            Row nodeRow = getRow(new Cell(1, node)).setResultIdx(1);
            List<String> cellsText = nodeRow.getCellsText(excludedColumns);
            listOfList.add(cellsText);
        }
        Row rowsEl = new Row(this).setTag("tr").setClasses("x-grid-tree-node-leaf");
        int rows = rowsEl.size();
        final List<Integer> columnsList = getColumns(columns, excludedColumns);
        List<List<String>> lists = getValues(rows, columnsList);
        listOfList.addAll(lists);
        return listOfList;
    }

    public List<List<String>> getCellsText(int... excludedColumns) {
        return getCellsText(false, t -> t == 0, Cell::getLanguages, excludedColumns);
    }

    public List<List<String>> getCellsText(boolean rowExpand, Predicate<Integer> predicate, Function<Cell, String> function, int... excludedColumns) {
        com.sdl.selenium.extjs6.grid.Row rowsEl = new com.sdl.selenium.extjs6.grid.Row(this).setTag("tr");
        com.sdl.selenium.extjs6.grid.Row rowEl = new com.sdl.selenium.extjs6.grid.Row(this, 1);
        if (rowExpand) {
            rowsEl.setExcludeClasses("x-grid-rowbody-tr");
            rowEl = new com.sdl.selenium.extjs6.grid.Row(this).setTag("tr").setExcludeClasses("x-grid-rowbody-tr").setResultIdx(1);
        }
        Cell columnsEl = new Cell(rowEl);
        int rows = rowsEl.size();
        int columns = columnsEl.size();
        final List<Integer> columnsList = getColumns(columns, excludedColumns);
        if (rows <= 0) {
            return null;
        } else {
            return getLists(rows, rowExpand, predicate, function, columnsList);
        }
    }

    private List<Integer> getColumns(int columns, int[] excludedColumns) {
        List<Integer> excluded = new ArrayList<>();
        for (int excludedColumn : excludedColumns) {
            excluded.add(excludedColumn);
        }

        List<Integer> columnsList = new ArrayList<>();
        for (int i = 1; i <= columns; i++) {
            if (!excluded.contains(i)) {
                columnsList.add(i);
            }
        }
        return columnsList;
    }

    private List<List<String>> getValues(int rows, List<Integer> columnsList) {
        Row rowsEl = new Row(this).setTag("tr");
        int size = rowsEl.size();
        List<List<String>> listOfList = new LinkedList<>();
        boolean canRead = true;
        String id = "";
        int timeout = 0;
        do {
            for (int i = 1; i <= rows; ++i) {
                if (canRead) {
                    List<String> list = new LinkedList<>();
                    for (int j : columnsList) {
                        Row row = new Row(this).setTag("tr").setResultIdx(i);
                        Cell cell = new Cell(row, j);
                        String text = cell.getText(true).trim();
                        list.add(text);
                    }
                    listOfList.add(list);
                } else {
                    if (size == i + 1) {
                        break;
                    }
                    Row row = new Row(this, i);
                    String currentId = row.getAttributeId();
                    if (!"".equals(id) && id.equals(currentId)) {
                        canRead = true;
                    }
                }
            }
            if (isScrollBottom()) {
                break;
            }
            Row row = new Row(this, size);
            id = row.getAttributeId();
            scrollPageDownInTree();
            canRead = false;
            timeout++;
        } while (timeout < 60);
        return listOfList;
    }

    private List<List<String>> getLists(int rows, boolean rowExpand, Predicate<Integer> predicate, Function<Cell, String> function, List<Integer> columnsList) {
        Row rowsEl = new Row(this);
        if (!rowExpand) {
            rowsEl.setTag("tr");
        }
        int size = rowsEl.size();
        List<List<String>> listOfList = new LinkedList<>();
        boolean canRead = true;
        String id = "";
        int timeout = 0;
        do {
            for (int i = 1; i <= rows; ++i) {
                if (canRead) {
                    List<String> list = new LinkedList<>();
                    for (int j : columnsList) {
                        Row row = new Row(this).setTag("tr").setResultIdx(i);
                        if (rowExpand) {
                            row.setExcludeClasses("x-grid-rowbody-tr");
                        }
                        Cell cell = new Cell(row, j);
                        String text;
                        if (predicate.test(j)) {
                            text = function.apply(cell);
                        } else {
                            text = cell.getText(true).trim();
                        }
                        list.add(text);
                    }
                    listOfList.add(list);
                } else {
                    if (size == i + 1) {
                        break;
                    }
                    Row row = new Row(this, i);
                    String currentId = row.getAttributeId();
                    if (!"".equals(id) && id.equals(currentId)) {
                        canRead = true;
                    }
                }
            }
            if (isScrollBottom()) {
                break;
            }
            Row row = new Row(this, size);
            id = row.getAttributeId();
            scrollPageDownInTree();
            canRead = false;
            timeout++;
        } while (timeout < 30);
        return listOfList;
    }

    public Row getRow(String searchElement, SearchType... searchTypes) {
        return new Row(this, searchElement, searchTypes).setInfoMessage("-Row");
    }

    public Row getRow(Cell... byCells) {
        return new Row(this, byCells).setInfoMessage("-Row");
    }

    static class Row extends com.sdl.selenium.extjs6.grid.Row {
        public Row() {
            super();
        }

        public Row(WebLocator grid) {
            super(grid);
        }

        public Row(WebLocator grid, int indexRow) {
            super(grid, indexRow);
        }

        public Row(WebLocator grid, String searchElement, SearchType... searchTypes) {
            super(grid, searchElement, searchTypes);
        }

        public Row(WebLocator grid, AbstractCell... cells) {
            super(grid, cells);
        }

        public Row(WebLocator grid, int indexRow, AbstractCell... cells) {
            super(grid, indexRow, cells);
        }
    }
}