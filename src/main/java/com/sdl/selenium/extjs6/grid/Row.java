package com.sdl.selenium.extjs6.grid;

import com.google.common.base.Strings;
import com.sdl.selenium.WebLocatorUtils;
import com.sdl.selenium.utils.config.WebDriverConfig;
import com.sdl.selenium.web.SearchType;
import com.sdl.selenium.web.WebLocator;
import com.sdl.selenium.web.table.AbstractCell;
import com.sdl.selenium.web.utils.RetryUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.time.Duration;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class Row extends com.sdl.selenium.web.table.Row {
    private static final Logger log = LogManager.getLogger(Row.class);
    private String version;

    public Row() {
        super();
        setTag("table");
    }

    public Row(WebLocator grid) {
        this();
        setContainer(grid);
    }

    public Row(WebLocator grid, int indexRow) {
        this(grid);
        setPosition(indexRow);
    }

    public Row(WebLocator grid, String searchElement, SearchType... searchTypes) {
        this(grid);
        setText(searchElement, searchTypes);
        if (isGridLocked()) {
            String index = getAttribute("data-recordindex");
            setTag("*");
            String xPath = getXPath().replace(grid.getXPath(), "");
            setElPath(xPath + "//table[@data-recordindex='" + index + "']");
        }
    }

    private boolean isGridLocked() {
        Grid grid = getGridAsContainer();
        if (grid == null) {
            return false;
        }
        String aClass;
        try {
            aClass = WebDriverConfig.getDriver() == null ? null : grid.getAttributeClass();
        } catch (NullPointerException e) {
            grid = (Grid) getPathBuilder().getContainer().getPathBuilder().getContainer();
            aClass = WebDriverConfig.getDriver() == null ? null : grid.getAttributeClass();
        }
        return aClass != null && aClass.contains("x-grid-locked");
    }

    private Grid getGridAsContainer() {
        Grid grid;
        if (getPathBuilder().getContainer() instanceof Grid) {
            grid = (Grid) getPathBuilder().getContainer();
        } else if (getPathBuilder().getContainer().getPathBuilder().getContainer() instanceof Grid) {
            grid = (Grid) getPathBuilder().getContainer().getPathBuilder().getContainer();
        } else if (getPathBuilder().getContainer().getPathBuilder().getContainer() != null && getPathBuilder().getContainer().getPathBuilder().getContainer().getPathBuilder().getContainer() instanceof Grid) {
            grid = (Grid) getPathBuilder().getContainer().getPathBuilder().getContainer().getPathBuilder().getContainer();
        } else {
            return null;
        }
        return grid;
    }

    public Row(WebLocator grid, AbstractCell... cells) {
        this(grid, false, cells);
    }

    public Row(WebLocator grid, boolean size, AbstractCell... cells) {
        this(grid);
        AbstractCell[] childNodes = Stream.of(cells).filter(t -> t != null && (t.getPathBuilder().getText() != null || (t.getPathBuilder().getChildNodes() != null && !t.getPathBuilder().getChildNodes().isEmpty()))).toArray(AbstractCell[]::new);
        if (isGridLocked()) {
            Integer index = null;
            if (!size) {
                List<Set<Integer>> ids = new ArrayList<>();
                for (AbstractCell cell : childNodes) {
                    ((Grid) grid).scrollTop();
                    Details details = getCellPosition(cell);
                    int indexCurrent = details.getLockedPosition();
                    cell.setTemplateValue("tagAndPosition", indexCurrent + "");
                    Row tmpEl;
                    if (details.getFirstColumns() >= details.getActualPosition()) {
                        WebLocator containerLocked = new WebLocator(grid).setClasses("x-grid-scrollbar-clipper", "x-grid-scrollbar-clipper-locked");
                        tmpEl = new Row(containerLocked).setChildNodes(cell);
                    } else {
                        WebLocator containerUnLocked = new WebLocator(grid).setClasses("x-grid-scrollbar-clipper").setExcludeClasses("x-grid-scrollbar-clipper-locked");
                        tmpEl = new Row(containerUnLocked).setChildNodes(cell);
                    }
                    boolean isScrollBottom;
                    Set<Integer> list = new LinkedHashSet<>();
                    do {
                        int count = tmpEl.size();
                        for (int j = 1; j <= count; j++) {
                            tmpEl.setResultIdx(j);
                            String indexValue = tmpEl.getAttribute("data-recordindex");
                            int i1 = Integer.parseInt(indexValue);
                            list.add(i1);
                        }
                        isScrollBottom = ((Grid) grid).isScrollBottom();
                        if (!isScrollBottom) {
                            ((Grid) grid).scrollPageDown();
                            ((Grid) grid).scrollPageDown();
                            ((Grid) grid).scrollPageDown();
                            ((Grid) grid).scrollPageDown();
                            ((Grid) grid).scrollPageDown();
                        }
                        tmpEl.setResultIdx(0);
                    } while (!isScrollBottom);
                    if (list.isEmpty()) {
                        log.info("No found child with value: '{}'", cell.getPathBuilder().getText());
                    }
                    ids.add(list);
                }
                ((Grid) grid).scrollTop();
                List<List<Integer>> collect = new ArrayList<>();
                for (Set<Integer> id : ids) {
                    List<Integer> strings = new ArrayList<>(id);
                    collect.add(strings);
                }
                List<Integer> theMinList = getTheMinList(collect);
                List<Integer> commonId = findCommonId(collect, theMinList);
                if (commonId.size() == 1) {
                    index = commonId.get(0);
                } else if (commonId.size() == 0) {
                    log.error("No found commonId!!!");
                } else {
                    log.error("Find more row that one!!!");
                }
            }
            for (AbstractCell cell : childNodes) {
                if (size) {
                    int indexCurrent = getCellPosition(cell).getLockedPosition();
                    cell.setTemplateValue("tagAndPosition", indexCurrent + "");
                } else {
                    cell.setTag("table[@data-recordindex='" + index + "']//td");
                }
            }
            setChildNodes(childNodes);
            if (size) {
                setTag("table");
            } else {
                setTag("*");
                setFinalXPath("//table[@data-recordindex='" + index + "']");
            }
        } else {
            setChildNodes(childNodes);
        }
    }

    private List<Integer> findCommonId(List<List<Integer>> ids, List<Integer> theMinList) {
        Set<Integer> intersection = new HashSet<>(theMinList);
        for (List<Integer> list : ids) {
            Set<Integer> newIntersection = new HashSet<>();
            for (Integer i : list) {
                if (intersection.contains(i)) {
                    newIntersection.add(i);
                }
            }
            intersection = newIntersection;
        }
        return new ArrayList<>(intersection);
    }

    private static List<Integer> getTheMinList(List<List<Integer>> lists) {
        int size = -1;
        for (List<Integer> list : lists) {
            int sizeTmp = list.size();
            if (size == -1) {
                size = sizeTmp;
            } else if (sizeTmp > 0) {
                if (sizeTmp < size) {
                    size = sizeTmp;
                }
            }
        }
        for (List<Integer> list : lists) {
            if (list.size() == size) {
                return list;
            }
        }
        return null;
    }

    private Details getCellPosition(AbstractCell cell) {
        int firstColumns = getLockedCells();
        String positions = cell.getPathBuilder().getTemplatesValues().get("tagAndPosition")[0];
        int actualPosition = Integer.parseInt(positions);
        int lockedPosition = getLockedPosition(firstColumns, actualPosition);
        return new Details(firstColumns, actualPosition, lockedPosition);
    }

    static class Details {
        private final int firstColumns;
        private final int actualPosition;
        private final int lockedPosition;

        public Details(int firstColumns, int actualPosition, int lockedPosition) {
            this.firstColumns = firstColumns;
            this.actualPosition = actualPosition;
            this.lockedPosition = lockedPosition;
        }

        public int getFirstColumns() {
            return firstColumns;
        }

        public int getActualPosition() {
            return actualPosition;
        }

        public int getLockedPosition() {
            return lockedPosition;
        }

        @Override
        public String toString() {
            return "Details{" +
                    "firstColumns=" + firstColumns +
                    ", actualPosition=" + actualPosition +
                    ", lockedPosition=" + lockedPosition +
                    '}';
        }
    }

    private int getLockedPosition(int firstColumns, int actualPosition) {
        if (actualPosition <= firstColumns) {
            return actualPosition;
        } else {
            return actualPosition - firstColumns;
        }
    }

    public Row(WebLocator grid, int indexRow, AbstractCell... cells) {
        this(grid, cells);
        setPosition(indexRow);
    }

    @Override
    public Cell getCell(int columnIndex) {
        if (isGridLocked()) {
            int firstColumns = getLockedCells();
            int position = 1;
            if (columnIndex > firstColumns) {
                columnIndex = columnIndex - firstColumns;
                position = 2;
            }
            return new Cell(this, columnIndex).setResultIdx(position);
        } else {
            return new Cell(this, columnIndex);
        }
    }

    private int getLockedCells() {
        Grid grid = getGridAsContainer();
        WebLocator containerLocked = new WebLocator(grid).setClasses("x-grid-scrollbar-clipper", "x-grid-scrollbar-clipper-locked");
        return new Row(containerLocked, 1).getCells();
    }

    public List<String> getCellsText(int... excludedColumns) {
        return getCellsText((short) 0, excludedColumns);
    }

    public <V> V getCellsText(Class<V> type, int... excludedColumns) {
        return getCellsText(type, (short) 0, excludedColumns);
    }

    public <V> V getCellsText(Class<V> type, short columnLanguages, int... excludedColumns) {
        return getCellsText(type, t -> t == columnLanguages, Cell::getLanguages, excludedColumns);
    }

    public <V> V getCellsText(Class<V> type, Predicate<Integer> predicate, Function<Cell, String> function, int... excludedColumns) {
        List<String> cellsText = getCellsText(predicate, function, excludedColumns);
        int fieldsCount;
        Constructor constructor = null;
        try {
            Class<?> newClazz = Class.forName(type.getTypeName());
            fieldsCount = cellsText.size();
            Constructor[] constructors = newClazz.getConstructors();
            for (Constructor c : constructors) {
                if (fieldsCount == c.getParameterCount()) {
                    constructor = c;
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        try {
            Constructor<V> constructorTemp = (Constructor<V>) constructor;
            return constructorTemp.newInstance(cellsText.toArray(new Object[0]));
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<String> getCellsText(short columnLanguages, int... excludedColumns) {
        if (isGridLocked()) {
            return getLockedCellsText(t -> t == columnLanguages, Cell::getLanguages, excludedColumns);
        } else {
            return getCellsText(t -> t == columnLanguages, Cell::getLanguages, excludedColumns);
        }
    }

    public List<String> getCellsText(Predicate<Integer> predicate, Function<Cell, String> function, int... excludedColumns) {
        WebLocator columnsEl = new WebLocator(this).setTag("td");
        List<Integer> columns = getColumns(columnsEl.size(), excludedColumns);
        List<String> list = new ArrayList<>();
        for (int j : columns) {
            Cell cell = new Cell(this, j);
            if (predicate.test(j)) {
                list.add(function.apply(cell));
            } else {
                list.add(cell.getText().trim());
            }
        }
        return list;
    }

    public List<String> getLockedCellsText(Predicate<Integer> predicate, Function<Cell, String> function, int... excludedColumns) {
        int firstColumns = getLockedCells();
        WebLocator columnsEl = new WebLocator(this).setTag("td");
        List<Integer> columns = getColumns(columnsEl.size(), excludedColumns);
        List<String> list = new ArrayList<>();
        WebLocator container = getPathBuilder().getContainer();
        WebLocator containerLocked = new WebLocator().setClasses("x-grid-scrollbar-clipper", "x-grid-scrollbar-clipper-locked");
        WebLocator containerUnLocked = new WebLocator().setClasses("x-grid-scrollbar-clipper").setExcludeClasses("x-grid-scrollbar-clipper-locked");
        String finalXPath = getPathBuilder().getFinalXPath();
        for (int j : columns) {
            if (j > firstColumns) {
                setFinalXPath(containerUnLocked.getXPath() + finalXPath);
            } else {
                setFinalXPath(containerLocked.getXPath() + finalXPath);
            }
            int currentPosition = getLockedPosition(firstColumns, j);
            Cell cell = new Cell(this, currentPosition);
            if (predicate.test(j)) {
                list.add(function.apply(cell));
            } else {
                list.add(cell.getText().trim());
            }
        }
        setFinalXPath(finalXPath);
        setContainer(container);
        return list;
    }

    private String getVersion() {
        if (version == null) {
            Grid grid = (Grid) getPathBuilder().getContainer();
            if (grid != null) {
                this.version = grid.getVersion();
            }
        }
        return version;
    }

    public <T extends Row> T setVersion(String version) {
        this.version = version;
        return (T) this;
    }

    public boolean select() {
        scrollInGrid();
        return isSelected() || selectPrivate() && isSelected();
    }

    public boolean doSelect() {
        scrollInGrid();
        return isSelected() || doSelectPrivate() && isSelected();
    }

    public boolean unSelect() {
        scrollInGrid();
        return !isSelected() || selectPrivate() && !isSelected();
    }

    public boolean doUnSelect() {
        scrollInGrid();
        return !isSelected() || doSelectPrivate() && !isSelected();
    }

    private boolean selectPrivate() {
        WebLocator checkBox = new WebLocator(this);
        if ("6.7.0".equals(getVersion())) {
            checkBox.setBaseCls("x-selmodel-column");
        } else {
            checkBox.setBaseCls("x-grid-row-checker");
        }
        return checkBox.click();
    }

    private boolean doSelectPrivate() {
        WebLocator checkBox = new WebLocator(this);
        if ("6.7.0".equals(getVersion())) {
            checkBox.setBaseCls("x-selmodel-column");
        } else {
            checkBox.setBaseCls("x-grid-row-checker");
        }
        return checkBox.doClick();
    }

    public boolean isSelected() {
        String aClass = getAttributeClass();
        return !Strings.isNullOrEmpty(aClass) && aClass.contains("x-grid-item-selected");
    }

    public boolean expand() {
        scrollInGrid();
        return !isCollapsed() || RetryUtils.retry(2, () -> doExpanded() && !isCollapsed());
    }

    public boolean collapse() {
        scrollInGrid();
        return isCollapsed() || RetryUtils.retry(2, () -> doExpanded() && isCollapsed());
    }

    protected boolean doExpanded() {
        WebLocator expander = new WebLocator(this).setBaseCls("x-grid-row-expander");
        return expander.doClick();
    }

    public boolean isCollapsed() {
        String aClass = getAttributeClass();
        return !Strings.isNullOrEmpty(aClass) && aClass.contains("x-grid-row-collapsed");
    }

    public boolean scrollTo() {
        return RetryUtils.retryIfNotSame(70, true, () -> {
            WebLocator container = getPathBuilder().getContainer();
            int lastRowVisibleIndex = new Row(container).findElements().size() - 1;
            Row row = new Row(container, lastRowVisibleIndex);
            WebLocatorUtils.scrollToWebLocator(row);
            return isPresent();
        });
    }

    public boolean scrollInGrid() {
        Grid grid = getGridAsContainer();
        if (grid.isScrollBottom()) {
            grid.scrollTop();
        }
        return RetryUtils.retry(100, () -> {
            boolean render = waitToRender(Duration.ofMillis(100), false) || grid.isScrollBottom();
            if (!render) {
                grid.scrollPageDown();
            }
            return render;
        });
    }

    public int getCells() {
        return new Cell(this).size();
    }

    public Row getNextRow() {
        return new Row(this).setRoot("/").setTag("following-sibling::table[1]");
    }

    public static void main(String[] args) {
        List<Integer> list1 = List.of(0, 2);
        List<Integer> list2 = List.of(0, 1, 2, 3);
        List<Integer> list3 = List.of(0, 1, 2, 3);
        List<Integer> list4 = List.of(1, 2);
        List<List<Integer>> list = List.of(list2, list3, list4);
        List<Integer> commonId = new Row().findCommonId(list, list1);
        log.info(commonId);
    }
}