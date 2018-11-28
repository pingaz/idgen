package seeds

import "flag"

var argSeed *Seed

func init() {
	argSeed = &Seed{
		*flag.Int("idgen.gen", 0, "generator id"),
		*flag.Uint("idgen.bits", 10, "generator bits"),
	}
}

func NewArgSeed() *Seed  {
	return argSeed
}
