package seeds

import (
	"github.com/go-redis/redis"
	"time"
)

type GoRedisAdapter struct {
	client *redis.ClusterClient
}

func NewGoRedisAdapter(addrs []string, pwd string) *GoRedisAdapter{
	return &GoRedisAdapter{
		redis.NewClusterClient(&redis.ClusterOptions{
			Addrs: addrs,
			Password: pwd, // no password set
		}),
	}
}

func (m *GoRedisAdapter) Get(key string) string  {
	str, err := m.client.Get(key).Result()
	if err == nil{
		return str
	}else{
		return ""
	}
}

func (m *GoRedisAdapter) Set(key string, value string, seconds uint) bool  {
	return m.client.Set(key, value, time.Duration(seconds) * time.Second).Err() == nil
}

func (m *GoRedisAdapter) Expire(key string, seconds uint) bool  {
	return m.client.Expire(key, time.Duration(seconds) * time.Second).Err() == nil
}

func (m *GoRedisAdapter) Del(key string) bool  {
	return m.client.Del(key).Err() == nil
}

func (m *GoRedisAdapter) Close(){
	m.client.Close()
}