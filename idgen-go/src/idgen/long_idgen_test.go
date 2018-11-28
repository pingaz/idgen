package idgen

import (
	"fmt"
	"testing"
	"time"
)

func TestLongUID(t *testing.T) {
	total := 1000 * 1000
	printCount := total / 10
	ids := make(map[int64]struct{})

	sf := NewLongUidGenerator(0, 10)

	prev := int64(0)
	last := time.Now()
	for i := 0; i < total; i++ {
		id := sf.NextLong()
		if i%printCount == 0 {
			fmt.Printf("Get id[%v] = %v \n", i, id)
		}

		if id <= prev {
			t.Errorf("Snowflake is not monotonic: %d <= %d", id, prev)
		}
		prev = id

		_, ok := ids[id]
		if ok {
			t.Errorf("Duplicate snowflake: %d", id)
		}
		ids[id] = struct{}{}
	}
	fmt.Printf("Used %v seconds to generate %v ids.\n", time.Now().Sub(last).Seconds(), total)
}

func TestNextTimeUnit(t *testing.T) {
	t1 := nowAsTimeUnit()
	t2 := nextTimeUnit(t1)

	if t2 <= t1 {
		t.Errorf("Time was not advanced to next time unit")
	}
}
