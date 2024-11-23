package com.gamecity.scrabble.model;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.gamecity.scrabble.service.impl.AbstractBoardTest;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

@ExtendWith(MockitoExtension.class)
class TestVirtualBoard extends AbstractBoardTest {

    @BeforeEach
    void beforeEach() {
        createBoardMatrix();
    }

    @Test
    void test_get_neighbour_cell() {
        createExistingHorizontalWord(6, 7, "DAMN");
        createExistingHorizontalWord(7, 3, "IMP");
        createExistingHorizontalWord(8, 5, "SRIS");
        createExistingHorizontalWord(9, 4, "PI");

        createExistingVerticalWord(7, 5, "PSIS");
        createExistingVerticalWord(6, 7, "DUI");
        createExistingVerticalWord(5, 8, "LA");

        final VirtualBoard virtualBoard = createVirtualBoard();
        final VirtualCell virtualCell = boardMatrix[7 - 1][3 - 1];

        assertThat(virtualBoard.getNeighbour(virtualCell, Direction.VERTICAL, ExtensionPoint.FORWARD), notNullValue());
    }

    @Test
    void test_has_sealed_neighbour_cell() {
        createExistingHorizontalWord(6, 7, "DAMN");
        createExistingHorizontalWord(7, 3, "IMP");
        createExistingHorizontalWord(8, 5, "SRIS");
        createExistingHorizontalWord(9, 4, "PI");

        createExistingVerticalWord(7, 5, "PSIS");
        createExistingVerticalWord(6, 7, "DUI");
        createExistingVerticalWord(5, 8, "LA");

        final VirtualBoard virtualBoard = createVirtualBoard();
        final VirtualCell virtualCell = boardMatrix[7 - 1][3 - 1];

        assertThat(virtualBoard.hasSealedNeighbour(virtualCell, Direction.VERTICAL, ExtensionPoint.FORWARD),
                equalTo(false));
    }

    @Test
    void test_has_not_used_neighbour_cell() {
        createExistingHorizontalWord(6, 7, "DAMN");
        createExistingHorizontalWord(7, 3, "IMP");
        createExistingHorizontalWord(8, 5, "SRIS");
        createExistingHorizontalWord(9, 4, "PI");

        createExistingVerticalWord(7, 5, "PSIS");
        createExistingVerticalWord(6, 7, "DUI");
        createExistingVerticalWord(5, 8, "LA");

        final VirtualBoard virtualBoard = createVirtualBoard();
        final VirtualCell virtualCell = boardMatrix[7 - 1][3 - 1];

        assertThat(virtualBoard.hasNotUsedNeighbour(virtualCell, Direction.VERTICAL, ExtensionPoint.FORWARD),
                equalTo(true));
    }

    @Test
    void test_has_available_neighbour_cell() {
        createExistingHorizontalWord(6, 7, "DAMN");
        createExistingHorizontalWord(7, 3, "IMP");
        createExistingHorizontalWord(8, 5, "SRIS");
        createExistingHorizontalWord(9, 4, "PI");

        createExistingVerticalWord(7, 5, "PSIS");
        createExistingVerticalWord(6, 7, "DUI");
        createExistingVerticalWord(5, 8, "LA");

        final VirtualBoard virtualBoard = createVirtualBoard();
        final VirtualCell virtualCell = boardMatrix[7 - 1][3 - 1];

        assertThat(virtualBoard.hasAvailableNeighbour(virtualCell, Direction.VERTICAL, ExtensionPoint.FORWARD),
                equalTo(true));
    }

    @Test
    void test_has_available_neighbour_cell_when_neighbour_has_sealed_neighbour() {
        createExistingHorizontalWord(6, 7, "DAMN");
        createExistingHorizontalWord(7, 3, "IMP");
        createExistingHorizontalWord(8, 5, "SRIS");
        createExistingHorizontalWord(9, 4, "PI");

        createExistingVerticalWord(7, 5, "PSIS");
        createExistingVerticalWord(6, 7, "DUI");
        createExistingVerticalWord(5, 8, "LA");

        final VirtualBoard virtualBoard = createVirtualBoard();
        final VirtualCell virtualCell = boardMatrix[8 - 1][3 - 1];

        assertThat(virtualBoard.hasAvailableNeighbour(virtualCell, Direction.VERTICAL, ExtensionPoint.FORWARD),
                equalTo(false));
    }

    private VirtualBoard createVirtualBoard() {
        return new VirtualBoard(Arrays.stream(boardMatrix).flatMap(Arrays::stream).collect(Collectors.toList()));
    }

}
