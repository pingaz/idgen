package idgen

import (
	"encoding/binary"
	"encoding/hex"
	"idgen/seeds"
)

type IdGenerator interface {
	NextBytes() []byte
	NextLong() int64
	NextHexString() string
}

type AbstractIdGenerator struct {
	IdGenerator
}

func (ig *AbstractIdGenerator) NextLong() (id int64) {
	return int64(binary.BigEndian.Uint64(ig.NextBytes()))
}

func (ig *AbstractIdGenerator) NextHexString() string {
	return hex.EncodeToString(ig.NextBytes())
}

func NewLongTimeBased() IdGenerator {
	return NewSeedLongTimeBased(seeds.NewArgSeed())
}

func NewSeedLongTimeBased(seed *seeds.Seed) IdGenerator {
	return NewLongUidGenerator(int64(seed.GeneratorId), seed.GeneratorBits)
}
