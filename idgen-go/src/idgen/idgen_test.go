package idgen

import (
	"encoding/binary"
	"fmt"
	"idgen/seeds"
	"testing"
)

func assertEqual(t *testing.T, a interface{}, b interface{}) {
	if a != b {
		t.Fatalf("%s != %s", a, b)
	}
}

func assertTrue(t *testing.T, a interface{}) {
	if a == false {
		t.Fatalf("not true")
	}
}

func assertLUID(t *testing.T, idgen IdGenerator) {
	bytes := idgen.NextBytes()
	assertEqual(t, len(bytes), 8)
	assertEqual(t, len(idgen.NextHexString()), 16)
}

func TestSimple(t *testing.T) {
	idgen := NewLongTimeBased()
	assertLUID(t, idgen)
}

func TestNetworkLongUID(t *testing.T) {
	assertLUID(t, NewSeedLongTimeBased(seeds.NewLocalIpAddressSeed(10)))
	for i := 0; i <= 24; i++ {
		max := ^(-1 << uint(i))
		for j := 0; j < ^(-1 << 24); j++ {
			networkAddressSeed := seeds.NewIpAddressSeed(j, uint(i))
			assertTrue(t,
				networkAddressSeed.GeneratorId == j ||
					networkAddressSeed.GeneratorId <= max ||
					networkAddressSeed.GeneratorId == 0)
		}
		fmt.Println("tested at bits: ", i)
	}
}

func TestDefaultLongUID(t *testing.T) {
	luid := NewLongTimeBased()
	number := luid.NextLong()
	fmt.Printf("number is %v.\n", number)

	bytes := luid.NextBytes()
	fmt.Printf("bytes number is %v.\n", binary.BigEndian.Uint64(bytes))
}

func TestSystemLongUID(t *testing.T) {
	luid := NewSeedLongTimeBased(seeds.NewArgSeed())
	number := luid.NextLong()
	fmt.Printf("number is %v.\n", number)

	bytes := luid.NextBytes()
	fmt.Printf("bytes number is %v.\n", binary.BigEndian.Uint64(bytes))
}
