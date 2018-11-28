package seeds

import (
	"encoding/binary"
	"errors"
	"net"
)

func NewLocalIpAddressSeed(generatorBits uint) *Seed {
	return NewIpAddressSeed(int(getLocalIP()), generatorBits)
}

func NewIpAddressSeed(ip int, generatorBits uint) *Seed {
	if generatorBits > maxGeneratorBit || generatorBits < 0 {
		panic(errors.New("generator bits can't be greater than 24 or less than 0"))
	}
	return &Seed{
		int((^(-1 << generatorBits)) & ip),
		generatorBits,
	}
}

func getLocalIP() uint32 {
	addrs, err := net.InterfaceAddrs()
	if err != nil {
		panic(err)
	}
	for _, address := range addrs {
		// check the address type and if it is not a loopback the display it
		if ipnet, ok := address.(*net.IPNet); ok && !ipnet.IP.IsLoopback() {
			if ipnet.IP.To4() != nil {
				return binary.BigEndian.Uint32(ipnet.IP)
			}
		}
	}
	panic(errors.New("Ip address not found "))
}
