package seeds

import (
	"strconv"
	"strings"
)

type RedisAdapter interface {
	Get(key string) string
	Set(key string, value string, expire uint) bool
	Expire(key string, seconds uint) bool
	Del(key string) bool
}

type RedisSeedRegister struct {
	adapter RedisAdapter
	from uint
	to uint
	expire uint
}

const defaultExpired = 7 * 24 * 60 * 60

func NewRedisSeedRegister(adapter RedisAdapter, from uint, to uint) *RedisSeedRegister {
	return &RedisSeedRegister{
		adapter, from, to, defaultExpired,
	}
}

func (r *RedisSeedRegister) Register(namespace string, seedId string) int {
	indexKey := getIndexKey(namespace, seedId)
	index := r.adapter.Get(indexKey)
	if index != ""{
		return r.Refresh(namespace, seedId, getGeneratorId(index))
	}else{
		for i:=r.from; i<=r.to; i++{
			key := getSeedKey(namespace, i)
			value := r.adapter.Get(key)
			if value=="" {
				reply := r.adapter.Set(key, seedId, r.expire)
				if reply {
					r.adapter.Set(indexKey, getGeneratorHex(i), r.expire)
					return int(i)
				}
			}else if value==seedId{
				return int(i)
			}
		}
	}
	return -1
}

func (r *RedisSeedRegister) Refresh(namespace string, seedId string, generatorId int) int{
	seedKey := getSeedKey(namespace, uint(generatorId))
	oldSeedId := r.adapter.Get(seedKey)
	if oldSeedId == seedId{
		r.adapter.Expire(seedKey, r.expire)
		r.adapter.Expire(getIndexKey(namespace, seedId), r.expire)
		return generatorId
	}else{
		return r.Register(namespace, seedId)
	}
}

func (r *RedisSeedRegister) Unregister(namespace string, seedId string, generatorId int){
	seedKey := getSeedKey(namespace, uint(generatorId))
	oldSeedId := r.adapter.Get(seedKey)
	if oldSeedId!="" && oldSeedId == seedId{
		r.adapter.Del(oldSeedId)
	}
	r.adapter.Del(seedKey)
}

func getIndexKey( namespace string,  seedId string ) string{
	var builder strings.Builder
	builder.WriteString(namespace)
	builder.WriteString(":index:")
	builder.WriteString(seedId)
	return builder.String()
}

func getSeedKey( namespace string,  index uint ) string{
	var builder strings.Builder
	builder.WriteString(namespace)
	builder.WriteString(":seed:")
	builder.WriteString(strconv.FormatInt(int64(index), 16))
	return builder.String()
}

func getGeneratorId(index string) int {
	i, _ := strconv.ParseInt(index, 0, 16)
	return int(i)
}

func getGeneratorHex(index uint) string {
	return strconv.FormatInt(int64(index), 16)
}