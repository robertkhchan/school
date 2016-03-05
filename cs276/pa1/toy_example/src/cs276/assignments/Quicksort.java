package cs276.assignments;

import java.util.List;

import cs276.util.Pair;

public class Quicksort  {
  private List<Pair<Integer,Integer>> numbers;
  private int number;

  public void sort(List<Pair<Integer,Integer>> values) {
    // check for empty or null array
    if (values ==null || values.size()==0){
      return;
    }
    this.numbers = values;
    number = values.size();
    quicksort(0, number - 1);
  }

  private void quicksort(int low, int high) {
    int i = low, j = high;
    // Get the pivot element from the middle of the list
    int pivot = numbers.get(low + (high-low)/2).getFirst();

    // Divide into two lists
    while (i <= j) {
      // If the current value from the left list is smaller then the pivot
      // element then get the next element from the left list
      while (numbers.get(i).getFirst() < pivot) {
        i++;
      }
      // If the current value from the right list is larger then the pivot
      // element then get the next element from the right list
      while (numbers.get(j).getFirst() > pivot) {
        j--;
      }

      // If we have found a values in the left list which is larger then
      // the pivot element and if we have found a value in the right list
      // which is smaller then the pivot element then we exchange the
      // values.
      // As we are done we can increase i and j
      if (i <= j) {
        exchange(i, j);
        i++;
        j--;
      }
    }
    // Recursion
    if (low < j)
      quicksort(low, j);
    if (i < high)
      quicksort(i, high);
  }

  private void exchange(int i, int j) {
    Pair<Integer,Integer> temp = numbers.get(i);
    numbers.set(i, numbers.get(j));
    numbers.set(j, temp);
  }
} 
