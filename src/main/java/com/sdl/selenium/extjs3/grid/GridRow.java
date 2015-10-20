package com.sdl.selenium.extjs3.grid;

import com.sdl.selenium.web.SearchType;
import com.sdl.selenium.web.WebLocator;
import com.sdl.selenium.web.table.Cell;
import com.sdl.selenium.web.table.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GridRow extends Row {
    private static final Logger LOGGER = LoggerFactory.getLogger(GridRow.class);

    public GridRow() {
        setRenderMillis(200);
        setClassName("GridRow");
        setTag("*");
    }

    //TODO generated standard xpath  independent of constructors
    public GridRow(GridPanel gridPanel) {
        this();
        setContainer(gridPanel);
        setTag("div");
        setClasses("x-grid3-row");
        setExcludeClasses("x-grid3-row-checker");
    }

    public GridRow(GridPanel gridPanel, int rowIndex) {
        setContainer(gridPanel);
        setTag("div[" + rowIndex + "]");
        setClasses("x-grid3-row");
        setExcludeClasses("x-grid3-row-checker");
    }

    public GridRow(GridPanel gridPanel, int searchColumnIndex, String searchElement, SearchType searchType) {
        this(gridPanel, new GridCell(searchColumnIndex, searchElement, searchType));
    }

    public GridRow(GridPanel gridPanel, String searchColumnId, String searchElement, SearchType searchType) {
        this(gridPanel);
        setTag("*");
        WebLocator cellEl = new WebLocator().setText(searchElement, searchType);
        setElPath("//" + getPathBuilder().getTag() + "[" + getSearchPaths(searchColumnId, cellEl) + "]");
    }

    public GridRow(GridPanel gridPanel, Cell... cells) {
        this(gridPanel);
        setTag("*");
        setChildNodes(cells);
    }

    private String getSearchPaths(String searchColumnId, WebLocator cellEl) {
        return "count(*[contains(@class, 'x-grid3-row-table')]//*[contains(@class, 'x-grid3-td-" + searchColumnId + "')]" + cellEl.getXPath() + ") > 0";
    }

    public GridRow(GridPanel gridPanel, int rowIndex, boolean isSelected) {
        this(gridPanel, rowIndex);
        if (isSelected) {
            setTag("div[" + rowIndex + "]");
            setClasses("x-grid3-row-selected");
            setExcludeClasses("x-grid3-row-checker");
        }
    }
}
