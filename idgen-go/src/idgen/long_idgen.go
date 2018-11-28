package idgen

import (
	"encoding/binary"
	"time"
)

type LongUIDGenerator struct {
	AbstractIdGenerator
	generatorId        int64
	generatorBit       uint
	generatorLeftShift uint
	maxCounter         int64
	timestampLeftShift uint
	generatorChannel   chan int64
}

func (lg *LongUIDGenerator) NextBytes() []byte {
	bs := make([]byte, 8)
	binary.LittleEndian.PutUint64(bs, uint64(lg.NextLong()))
	return bs
}

func (lg *LongUIDGenerator) NextLong() int64 {
	return <-lg.generatorChannel
}

func NewLongUidGenerator(id int64, bits uint) IdGenerator {
	var idgen = &LongUIDGenerator{}
	idgen.AbstractIdGenerator.IdGenerator = idgen
	idgen.generatorBit = bits
	idgen.generatorId = id
	counterBits := uint(64 - timestampBits - bits - 1)
	idgen.maxCounter = 1<<counterBits - 1
	idgen.timestampLeftShift = counterBits + bits
	idgen.generatorLeftShift = counterBits
	idgen.generatorChannel = make(chan int64)
	go longGenerator(idgen)
	return idgen
}

const timestampBits = 35
const timestampRightShift = 7
const twepoch = 1466064376616

func longGenerator(lg *LongUIDGenerator) {
	last := nowAsTimeUnit()
	seq := int64(0)
	for {
		ts := nowAsTimeUnit()
		if ts < last {
			ts = nextTimeUnit(last)
		}

		if ts != last {
			seq = 0
			last = ts
		} else if seq == lg.maxCounter {
			ts = nextTimeUnit(ts)
			seq = 0
			last = ts
		} else {
			seq++
		}

		id := (ts << lg.timestampLeftShift) | (lg.generatorId << lg.generatorLeftShift) | seq
		lg.generatorChannel <- id
	}
}

func nextTimeUnit(ts int64) int64 {
	i := nowAsTimeUnit()
	for ; i <= ts; i = nowAsTimeUnit() {
		time.Sleep(time.Millisecond * time.Duration(nextMills(ts)))
	}
	return i
}

func nowAsTimeUnit() int64 {
	return (time.Now().UnixNano()/1e6 - twepoch) >> timestampRightShift
}

func nextMills(ts int64) int64 {
	return ((ts + 1) << timestampRightShift) - (time.Now().UnixNano()/1e6 - twepoch)
}
