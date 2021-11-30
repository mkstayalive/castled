---
sidebar_position: 1
---

# Connecting via SSH Tunnel

Castled can connect to your data warehouse/destination apps by creating a tunnel via one of your **ssh/jump** hosts. Please provide the following information to enable ssh tunneling.

* **SSH Host** - IP Address/Host name of the ssh server

* **SSH Port** - SSH port on the ssh server. Default is 22.

* **SSH User** - Username of the ssh host

## Add Castled Public Keys to SSH Server

Add the castled public key to the authorized_keys files of the ssh server

```
echo 'ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABgQDSCaSC+kbnp0MPn0Xr3bcxF7P6rVl+MkUCbL4RgO85kcULy2tacmunTdzSEncisuloM9TmZzWLjcb/gz3HlCCouoFRZSI0XGLoeQ8jUziUg7GPD1AVKHzL+srGzx8az002fu6qDWP6m051rnd99So4e6rhBJx1+mJUotWGecSQnlQAAdBtiS3qcVxM9brHbTA8lDMWm4019F8Ak5TefQX767WduNhFr7wQdpgkCb9k2u8seDVaXwZoV2BdLUOVGWZb/RGogKzCKPO4reBDgZxGofB0OIH8SsswXqbDU7lIdBO/fGQaVPT7y4YwDvqsFPBE0e1bvRKV4fj6/DBQtz9Oeb3kSwx0/8EMrQCV9szGHp04DL3HbWbtMpLjyP6Ywjzhrat61qjghCs96Z9KoHe3hhiU29whd0nkya3cq0jXrKzVg8d9c1eZTs1LBu/wxox4DqJV/BbfksSePhnvvts750/slNWyEAckXcysofNWxvA/1I4rfNz1QbpT8+BujwE= tunnel' >> ~/.ssh/authorized_keys
```
